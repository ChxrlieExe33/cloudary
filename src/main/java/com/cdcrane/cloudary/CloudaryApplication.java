package com.cdcrane.cloudary;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CloudaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudaryApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner() {
        return args -> {
            System.out.println("\n-----------------------------------------------");
            System.out.println("             Cloudary is running!              ");
            System.out.println("-----------------------------------------------\n");
        };
    }
}
