package com.ibmec.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class IbmecMallApplication {
    public static void main(String[] args) {
        SpringApplication.run(IbmecMallApplication.class, args);
    }
} 