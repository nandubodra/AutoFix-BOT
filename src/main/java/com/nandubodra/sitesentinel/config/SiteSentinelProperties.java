package com.nandubodra.sitesentinel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sitesentinel")
public class SiteSentinelProperties {

    private final Monitoring monitoring = new Monitoring();
    private final Http http = new Http();
    private final Security security = new Security();
    private String appBaseUrl;

    public Monitoring getMonitoring() { return monitoring; }
    public Http getHttp() { return http; }
    public Security getSecurity() { return security; }
    public String getAppBaseUrl() { return appBaseUrl; }
    public void setAppBaseUrl(String appBaseUrl) { this.appBaseUrl = appBaseUrl; }

    public static class Monitoring {
        private long schedulerDelayMs;
        private int claimSeconds;
        private int threadPoolSize;
        public long getSchedulerDelayMs() { return schedulerDelayMs; }
        public void setSchedulerDelayMs(long schedulerDelayMs) { this.schedulerDelayMs = schedulerDelayMs; }
        public int getClaimSeconds() { return claimSeconds; }
        public void setClaimSeconds(int claimSeconds) { this.claimSeconds = claimSeconds; }
        public int getThreadPoolSize() { return threadPoolSize; }
        public void setThreadPoolSize(int threadPoolSize) { this.threadPoolSize = threadPoolSize; }
    }

    public static class Http {
        private int maxRedirects;
        private int maxResponseBytes;
        public int getMaxRedirects() { return maxRedirects; }
        public void setMaxRedirects(int maxRedirects) { this.maxRedirects = maxRedirects; }
        public int getMaxResponseBytes() { return maxResponseBytes; }
        public void setMaxResponseBytes(int maxResponseBytes) { this.maxResponseBytes = maxResponseBytes; }
    }

    public static class Security {
        private int loginWindowSeconds;
        private int maxLoginAttempts;
        public int getLoginWindowSeconds() { return loginWindowSeconds; }
        public void setLoginWindowSeconds(int loginWindowSeconds) { this.loginWindowSeconds = loginWindowSeconds; }
        public int getMaxLoginAttempts() { return maxLoginAttempts; }
        public void setMaxLoginAttempts(int maxLoginAttempts) { this.maxLoginAttempts = maxLoginAttempts; }
    }
}
