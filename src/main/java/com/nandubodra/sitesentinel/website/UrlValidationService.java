package com.nandubodra.sitesentinel.website;

import org.springframework.stereotype.Service;

import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class UrlValidationService {

    public String normalizeHttpsUrl(String input) {
        String raw = input == null ? "" : input.trim();
        if (raw.isBlank()) {
            throw new IllegalArgumentException("URL is required");
        }

        URI uri;
        try {
            uri = new URI(raw);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Invalid URL format");
        }

        if (uri.getScheme() == null || !"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("Only HTTPS URLs are supported");
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("URL host is required");
        }

        if (uri.getUserInfo() != null) {
            throw new IllegalArgumentException("User info in URL is not supported");
        }

        if (uri.getFragment() != null) {
            throw new IllegalArgumentException("URL fragments are not supported");
        }

        String host = IDN.toASCII(uri.getHost());
        int port = uri.getPort();
        if (port != -1 && port != 443) {
            throw new IllegalArgumentException("Only default HTTPS port is supported");
        }

        String path = uri.getRawPath();
        if (path == null || path.isBlank()) {
            path = "/";
        }

        URI normalized = URI.create("https://" + host.toLowerCase() + path +
                (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery()));
        return normalized.toString();
    }
}
