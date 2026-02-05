package com.cdcrane.cloudary;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class CloudaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudaryApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner() {
        return args -> {
            System.out.println("-----------------------------------------------");
            System.out.println("             Cloudary is running!              ");
            System.out.println("-----------------------------------------------\n");
        };
    }
}
