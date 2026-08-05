package com.sprint.mission.discodeit.storage.s3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "discodeit.storage.s3")
public class S3Properties {

  private String region = "ap-northeast-2";

  private String bucket;

  private long presignedUrlExpiration = 600;

}
