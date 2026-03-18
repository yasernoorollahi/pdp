package com.datarain.pdp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.datarain.pdp", "com.pdp"})
@EntityScan(basePackages = {"com.datarain.pdp", "com.pdp"})
@EnableJpaRepositories(basePackages = {"com.datarain.pdp", "com.pdp"})
@EnableScheduling
// اضافه شد: @EnableAsync برای سرویس‌های audit که از @Async استفاده میکنن
@EnableAsync
public class PersonalDataPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersonalDataPlatformApplication.class, args);
    }
}
