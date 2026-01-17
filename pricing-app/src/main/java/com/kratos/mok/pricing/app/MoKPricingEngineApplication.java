package com.kratos.mok.pricing.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ConfigurationPropertiesScan(basePackages = "com.kratos.mok.pricing")
public class MoKPricingEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoKPricingEngineApplication.class, args);
    }

}
