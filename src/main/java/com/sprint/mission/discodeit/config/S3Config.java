package com.sprint.mission.discodeit.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
@EnableConfigurationProperties(S3Property.class) // 설정값 별도 클래스 사용
public class S3Config {

    // use aws api
    @Bean
    public S3Client s3Client(S3Property prop) {

        S3ClientBuilder builder = S3Client.builder();

        builder.region(Region.of(prop.getRegion()));
        setEndpointWithClient(prop, builder);

        return builder.build();
    }

    // create presigned url from signature.
    @Bean
    public S3Presigner s3Presigner(S3Property prop) {
        S3Presigner.Builder builder = S3Presigner.builder();

        builder.region(Region.of(prop.getRegion()));
        setEndpointWithPresigner(prop, builder);

        return builder.build();
    }

    private void setEndpointWithPresigner(S3Property prop, S3Presigner.Builder builder) {
        if (StringUtils.hasText(prop.getEndpoint())) {
            builder.endpointOverride(URI.create(prop.getEndpoint())) // test url로 endpoint를 설정
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build())
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("test", "test")));
        } else {
            // endpoint에 값이 없다면 진짜 AWS에 요청을 보내야 하는 상황
            builder.serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(false)
                            .build())
                    .credentialsProvider(DefaultCredentialsProvider.builder().build());
        }
    }


    private void setEndpointWithClient(S3Property prop, S3ClientBuilder builder) {
        // url type.
        // - path string : local(mockup) or minio or legacy
        // - virtual string : aws s3
        if (StringUtils.hasText(prop.getEndpoint())) {
            // if has endpoint, set up to local test
            builder.endpointOverride(URI.create(prop.getEndpoint()))
                    .forcePathStyle(true)
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(
                                            "accessKey",
                                            "secretKey"
                                    )
                            )
                    );
        } else {
            // no endpoint, setup to real s3
            builder.forcePathStyle(false)
                    .credentialsProvider(
                            DefaultCredentialsProvider.builder().build()
                    );
        }
    }

}
