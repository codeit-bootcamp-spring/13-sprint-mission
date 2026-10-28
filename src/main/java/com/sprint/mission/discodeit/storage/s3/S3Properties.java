package com.sprint.mission.discodeit.storage.s3;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Setter
@ConfigurationProperties(prefix = "discodeit.storage.s3")
public class S3Properties {

  @NotBlank // 애플리케이션 시작 시점에 버킷 누락 발견
  private String bucket;

  @NotBlank
  private String region = "ap-northeast-2";

  private String endpoint;

  @Min(1)
  private int presignedUrlExpiration = 600;

}
