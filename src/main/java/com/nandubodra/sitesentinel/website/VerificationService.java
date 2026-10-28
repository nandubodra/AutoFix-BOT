package com.nandubodra.sitesentinel.website;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

@Service
public class VerificationService {

    private final HttpClient httpClient;

    public VerificationService(HttpClient monitoringHttpClient) {
        this.httpClient = monitoringHttpClient;
    }

    public String createToken() {
        return UUID.randomUUID().toString();
    }

    public boolean verify(Website website) {
        URI target = URI.create(website.getNormalizedUrl())
                .resolve("/.well-known/sitesentinel-verification/" + website.getVerificationToken());

        HttpRequest request = HttpRequest.newBuilder(target)
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 && response.body().trim().equals(website.getVerificationToken());
        } catch (IOException | InterruptedException ignored) {
            if (ignored instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
}
