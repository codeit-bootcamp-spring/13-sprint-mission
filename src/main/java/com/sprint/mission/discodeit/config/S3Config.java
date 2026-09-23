package com.sprint.mission.discodeit.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {


    @Bean
    public S3Client s3Client(S3Properties props) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(props.getRegion()));


        builder.forcePathStyle(false)
                .credentialsProvider(DefaultCredentialsProvider.builder().build());
        log.info("S3Client - 실제 AWS(region={})", props.getRegion());

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner(S3Properties props) {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(props.getRegion()));

        builder.serviceConfiguration(S3Configuration.builder()
                .pathStyleAccessEnabled(false)
                .build())
                .credentialsProvider(DefaultCredentialsProvider.builder().build());

        return builder.build();
    }
}
