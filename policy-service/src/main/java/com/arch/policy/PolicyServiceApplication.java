package com.arch.policy;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.SpringApplication;

@SpringBootApplication
@ConfigurationPropertiesScan   // binds @ConfigurationProperties records (ServiceProperties)
public class PolicyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PolicyServiceApplication.class, args);
    }
}
