package com.cdcrane.cloudary.config.internal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsResourceConfig {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.EU_WEST_3)
                .build();
    }
}
