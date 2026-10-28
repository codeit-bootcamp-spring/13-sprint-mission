package com.sprint.mission.discodeit.storage.s3;

import java.net.URI;
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

@Configuration
@Slf4j
@ConditionalOnProperty(prefix = "discodeit.storage", name = "type", havingValue = "s3")
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

  @Bean
  public S3Client s3Client(S3Properties properties) {
    S3ClientBuilder builder = S3Client.builder()
        .region(Region.of(properties.getRegion())); // 서울 region

    if (StringUtils.hasText(properties.getEndpoint())) { // 테스트 url로 엔드 포인트 설정
      builder.endpointOverride(URI.create(properties.getEndpoint()))
          .forcePathStyle(true) // 경로 방식 사용
          .credentialsProvider(
              StaticCredentialsProvider.create(
                  AwsBasicCredentials.create("test", "test"))); // 임의의 값으로 자격 증명 설정

      log.info("S3Client - 로컬 Mock 사용: {}", properties.getEndpoint());

    } else { // endpoint에 값이 없다면 진짜 AWS에 요청을 보내야 하는 상황
      builder.forcePathStyle(false) // 가상 호스팅
          .credentialsProvider(DefaultCredentialsProvider.builder().build()); // 기본 자격 증명 체인 사용

      log.info("S3Client - 실제 AWS 사용: region={}", properties.getRegion());
    }
    return builder.build();
  }

  @Bean
  public S3Presigner s3Presigner(S3Properties properties) {
    S3Presigner.Builder builder = S3Presigner.builder().region(Region.of(properties.getRegion()));

    if (StringUtils.hasText(properties.getEndpoint())) {
      builder.endpointOverride(URI.create(properties.getEndpoint()))
          .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
          .credentialsProvider(
              StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")));

    } else {
      builder.serviceConfiguration(
              S3Configuration.builder().pathStyleAccessEnabled(false).build())
          .credentialsProvider(DefaultCredentialsProvider.builder().build());
    }

    return builder.build();
  }
}
