package com.nandubodra.sitesentinel.alert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AlertDeliveryRepository extends JpaRepository<AlertDelivery, Long> {
    Optional<AlertDelivery> findByDedupKey(String dedupKey);

    List<AlertDelivery> findTop10ByStatusOrderByCreatedAtDesc(DeliveryStatus status);

    List<AlertDelivery> findTop50ByStatusInAndNextAttemptAtBeforeOrderByCreatedAtAsc(
            List<DeliveryStatus> statuses, Instant cutoff);
}
