package com.example.coalawebbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CoalaWebBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoalaWebBackendApplication.class, args);
    }

}
