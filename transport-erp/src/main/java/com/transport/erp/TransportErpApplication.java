package com.transport.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TransportErpApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransportErpApplication.class, args);
    }

}
