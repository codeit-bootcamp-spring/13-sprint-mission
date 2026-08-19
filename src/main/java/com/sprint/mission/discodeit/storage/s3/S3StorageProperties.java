package com.sprint.mission.discodeit.storage.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * S3 저장소 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "discodeit.storage.s3")
public record S3StorageProperties(
        String accessKey,
        String secretKey,
        String region,
        String bucket,
        Integer presignedUrlExpiration
) {
}