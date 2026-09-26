package com.kart.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class KartDeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(KartDeliveryApplication.class, args);
    }

}
