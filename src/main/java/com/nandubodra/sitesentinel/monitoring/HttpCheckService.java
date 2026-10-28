package com.nandubodra.sitesentinel.monitoring;

import com.nandubodra.sitesentinel.config.SiteSentinelProperties;
import com.nandubodra.sitesentinel.website.Website;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Service
public class HttpCheckService {

    private final HttpClient httpClient;
    private final HostSafetyService hostSafetyService;
    private final SiteSentinelProperties properties;

    public HttpCheckService(HttpClient monitoringHttpClient,
                            HostSafetyService hostSafetyService,
                            SiteSentinelProperties properties) {
        this.httpClient = monitoringHttpClient;
        this.hostSafetyService = hostSafetyService;
        this.properties = properties;
    }

    public HttpCheckOutcome check(Website website) {
        Instant start = Instant.now();
        URI current = URI.create(website.getNormalizedUrl());
        Set<String> visited = new HashSet<>();
        int redirects = 0;

        while (true) {
            visited.add(current.toString());
            hostSafetyService.assertSafeHost(current.getHost());

            HttpRequest request = HttpRequest.newBuilder(current)
                    .GET()
                    .timeout(Duration.ofMillis(website.getTimeoutMs()))
                    .build();

            HttpResponse<InputStream> response;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            } catch (IOException ex) {
                return HttpCheckOutcome.failed("NETWORK", "Network failure", elapsed(start), null);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return HttpCheckOutcome.failed("INTERRUPTED", "Check interrupted", elapsed(start), null);
            }

            try {
                consumeWithLimit(response.body(), properties.getHttp().getMaxResponseBytes());
            } catch (IOException ex) {
                return HttpCheckOutcome.failed("RESPONSE_TOO_LARGE", "Response exceeded limit", elapsed(start), null);
            }

            int status = response.statusCode();
            if (isRedirect(status)) {
                if (redirects >= properties.getHttp().getMaxRedirects()) {
                    return HttpCheckOutcome.failed("REDIRECT", "Too many redirects", elapsed(start), status);
                }
                String location = response.headers().firstValue("location").orElse(null);
                if (location == null || location.isBlank()) {
                    return HttpCheckOutcome.failed("REDIRECT", "Redirect without location", elapsed(start), status);
                }
                URI target = current.resolve(location);
                if (!"https".equalsIgnoreCase(target.getScheme())) {
                    return HttpCheckOutcome.failed("REDIRECT", "Redirected to unsupported scheme", elapsed(start), status);
                }
                if (!visited.add(target.toString())) {
                    return HttpCheckOutcome.failed("REDIRECT", "Redirect loop detected", elapsed(start), status);
                }
                current = target;
                redirects++;
                continue;
            }

            boolean success = parseExpectedStatuses(website.getExpectedStatuses()).contains(status);
            if (success) {
                return HttpCheckOutcome.success(elapsed(start), status);
            }
            return HttpCheckOutcome.failed("STATUS", "Unexpected HTTP status", elapsed(start), status);
        }
    }

    private void consumeWithLimit(InputStream inputStream, int maxBytes) throws IOException {
        byte[] buffer = new byte[4096];
        int total = 0;
        while (true) {
            int read = inputStream.read(buffer);
            if (read == -1) {
                return;
            }
            total += read;
            if (total > maxBytes) {
                throw new IOException("response too large");
            }
        }
    }

    private boolean isRedirect(int status) {
        return status == 301 || status == 302 || status == 303 || status == 307 || status == 308;
    }

    Set<Integer> parseExpectedStatuses(String expectedStatuses) {
        Set<Integer> statuses = new HashSet<>();
        for (String part : expectedStatuses.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            statuses.add(Integer.parseInt(trimmed));
        }
        if (statuses.isEmpty()) {
            statuses.add(200);
        }
        return statuses;
    }

    private long elapsed(Instant start) {
        return Duration.between(start, Instant.now()).toMillis();
    }
}
