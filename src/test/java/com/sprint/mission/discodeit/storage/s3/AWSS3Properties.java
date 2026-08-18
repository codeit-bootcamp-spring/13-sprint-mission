package com.sprint.mission.discodeit.storage.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AWS S3 테스트에 필요한 설정값을 관리한다.
 *
 * 실제 Access Key와 Secret Key는 포함하지 않는다.
 * 로컬 환경에서는 AWS CLI의 discodeit 프로필을 사용한다.
 */

@ConfigurationProperties(prefix = "aws.s3")
public record AWSS3Properties(
    String profile,
    String region,
    String bucket
) {
}

