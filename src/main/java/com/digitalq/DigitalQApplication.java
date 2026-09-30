package com.digitalq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DigitalQApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalQApplication.class, args);
    }
}
