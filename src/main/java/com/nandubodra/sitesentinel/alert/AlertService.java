package com.nandubodra.sitesentinel.alert;

import com.nandubodra.sitesentinel.incident.Incident;
import com.nandubodra.sitesentinel.incident.IncidentStatus;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AlertService {

    private final AlertDeliveryRepository alertDeliveryRepository;
    private final JavaMailSender mailSender;

    public AlertService(AlertDeliveryRepository alertDeliveryRepository, JavaMailSender mailSender) {
        this.alertDeliveryRepository = alertDeliveryRepository;
        this.mailSender = mailSender;
    }

    @Transactional
    public void sendIncidentAlert(Incident incident) {
        enqueueAndSend(incident, AlertType.INCIDENT);
    }

    @Transactional
    public void sendRecoveryAlert(Incident incident) {
        enqueueAndSend(incident, AlertType.RECOVERY);
    }

    @Transactional
    public void retryPending() {
        List<AlertDelivery> deliveries = alertDeliveryRepository.findTop50ByStatusInAndNextAttemptAtBeforeOrderByCreatedAtAsc(
                List.of(DeliveryStatus.RETRY_SCHEDULED), Instant.now());
        for (AlertDelivery delivery : deliveries) {
            attemptSend(delivery);
        }
    }

    private void enqueueAndSend(Incident incident, AlertType type) {
        String dedupKey = incident.getId() + ":" + type;
        if (alertDeliveryRepository.findByDedupKey(dedupKey).isPresent()) {
            return;
        }

        AlertDelivery delivery = new AlertDelivery();
        delivery.setIncident(incident);
        delivery.setWebsite(incident.getWebsite());
        delivery.setAlertType(type);
        delivery.setStatus(DeliveryStatus.PENDING);
        delivery.setRecipientEmail(incident.getWebsite().getOwner().getEmail());
        delivery.setDedupKey(dedupKey);
        delivery.setAttemptCount(0);
        alertDeliveryRepository.save(delivery);

        attemptSend(delivery);
    }

    private void attemptSend(AlertDelivery delivery) {
        delivery.setAttemptCount(delivery.getAttemptCount() + 1);
        delivery.setLastAttemptAt(Instant.now());

        try {
            if (mailSender == null) {
                throw new IllegalStateException("Mail sender unavailable");
            }
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false);
            helper.setTo(delivery.getRecipientEmail());
            helper.setSubject(subject(delivery));
            helper.setText(body(delivery));
            mailSender.send(message);
            delivery.setStatus(DeliveryStatus.SENT);
            delivery.setErrorSummary(null);
            delivery.setNextAttemptAt(null);
        } catch (Exception ex) {
            if (delivery.getAttemptCount() >= 3) {
                delivery.setStatus(DeliveryStatus.FAILED);
            } else {
                delivery.setStatus(DeliveryStatus.RETRY_SCHEDULED);
                delivery.setNextAttemptAt(Instant.now().plusSeconds((long) delivery.getAttemptCount() * 60));
            }
            delivery.setErrorSummary(sanitize(ex.getMessage()));
        }
        alertDeliveryRepository.save(delivery);
    }

    private String subject(AlertDelivery delivery) {
        return delivery.getAlertType() == AlertType.INCIDENT
                ? "[SiteSentinel] Incident: " + delivery.getWebsite().getName()
                : "[SiteSentinel] Recovery: " + delivery.getWebsite().getName();
    }

    private String body(AlertDelivery delivery) {
        Incident incident = delivery.getIncident();
        String state = incident.getStatus() == IncidentStatus.RESOLVED ? "resolved" : "detected";
        return "Website: " + delivery.getWebsite().getName() + "\n"
                + "URL: " + delivery.getWebsite().getNormalizedUrl() + "\n"
                + "Incident: " + state + "\n"
                + "Description: " + incident.getDescription();
    }

    private String sanitize(String raw) {
        if (raw == null) {
            return "Delivery failed";
        }
        String value = raw.replaceAll("[\r\n]", " ").trim();
        if (value.length() > 480) {
            value = value.substring(0, 480);
        }
        return value;
    }
}
