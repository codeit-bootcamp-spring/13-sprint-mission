package com.sprint.mission.discodeit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("discodeit.storage.aws.s3")
public class S3Property {
    private String bucket;
    private String region;

    // endpoint. null == aws | else == local
    private String endpoint;

    // presigned Url activation time
    private int presignTime;
}
