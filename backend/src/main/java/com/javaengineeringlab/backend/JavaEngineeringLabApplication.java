package com.javaengineeringlab.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JavaEngineeringLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaEngineeringLabApplication.class, args);
    }
}
