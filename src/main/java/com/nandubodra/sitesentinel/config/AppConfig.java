package com.nandubodra.sitesentinel.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties(SiteSentinelProperties.class)
public class AppConfig {

    @Bean(destroyMethod = "shutdown")
    ExecutorService monitorExecutor(SiteSentinelProperties properties) {
        return Executors.newFixedThreadPool(properties.getMonitoring().getThreadPoolSize());
    }

    @Bean
    HttpClient monitoringHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }
}
