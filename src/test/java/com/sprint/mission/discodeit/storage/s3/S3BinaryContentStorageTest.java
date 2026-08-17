package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import com.sprint.mission.discodeit.storage.LocalBinaryContentStorage;
import com.sprint.mission.discodeit.storage.S3BinaryContentStorage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

class S3BinaryContentStorageTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(TestConfiguration.class);

    @Configuration
    @Import({
            LocalBinaryContentStorage.class,
            S3BinaryContentStorage.class
    })
    static class TestConfiguration {
    }

    @Test
    void storageType이S3이면S3BinaryContentStorage가등록된다() {

        contextRunner
                .withPropertyValues(
                        "discodeit.storage.type=s3",
                        "discodeit.storage.local.root-path=.discodeit/storage",
                        "discodeit.storage.s3.access-key=test-access-key",
                        "discodeit.storage.s3.secret-key=test-secret-key",
                        "discodeit.storage.s3.region=ap-northeast-2",
                        "discodeit.storage.s3.bucket=test-bucket",
                        "discodeit.storage.s3.presigned-url-expiration=600"
                )
                .run(context -> {

                    assertThat(context)
                            .hasSingleBean(BinaryContentStorage.class);

                    assertThat(context)
                            .hasSingleBean(S3BinaryContentStorage.class);

                    assertThat(context)
                            .doesNotHaveBean(LocalBinaryContentStorage.class);
                });
    }

    @Test
    void storageType이Local이면LocalBinaryContentStorage가등록된다() {

        contextRunner
                .withPropertyValues(
                        "discodeit.storage.type=local",
                        "discodeit.storage.local.root-path=.discodeit/storage"
                )
                .run(context -> {

                    assertThat(context)
                            .hasSingleBean(BinaryContentStorage.class);

                    assertThat(context)
                            .hasSingleBean(LocalBinaryContentStorage.class);

                    assertThat(context)
                            .doesNotHaveBean(S3BinaryContentStorage.class);
                });
    }

    @Test
    void storageType설정이없으면LocalStorage가기본값으로등록된다() {

        contextRunner
                .withPropertyValues(
                        "discodeit.storage.local.root-path=.discodeit/storage"
                )
                .run(context -> {

                    assertThat(context)
                            .hasSingleBean(BinaryContentStorage.class);

                    assertThat(context)
                            .hasSingleBean(LocalBinaryContentStorage.class);

                    assertThat(context)
                            .doesNotHaveBean(S3BinaryContentStorage.class);
                });
    }
}