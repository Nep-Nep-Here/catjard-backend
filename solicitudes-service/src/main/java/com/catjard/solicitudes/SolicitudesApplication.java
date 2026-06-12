package com.catjard.solicitudes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@ConfigurationPropertiesScan
@EnableScheduling
public class SolicitudesApplication {
    public static void main(String[] args) {
        SpringApplication.run(SolicitudesApplication.class, args);
    }
}
