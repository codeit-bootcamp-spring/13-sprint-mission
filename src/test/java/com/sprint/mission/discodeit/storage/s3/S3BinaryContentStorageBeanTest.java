package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import com.sprint.mission.discodeit.storage.LocalBinaryContentStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("BinaryContentStorage 조건부 Bean 등록 테스트")
class S3BinaryContentStorageBeanTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withBean(
                            S3Client.class,
                            () -> mock(S3Client.class)
                    )
                    .withBean(
                            S3Presigner.class,
                            () -> mock(S3Presigner.class)
                    )
                    .withBean(
                            S3Properties.class,
                            () -> new S3Properties(
                                    "ap-northeast-2",
                                    "test-bucket",
                                    600
                            )
                    )
                    .withUserConfiguration(
                            S3BinaryContentStorage.class,
                            LocalBinaryContentStorage.class
                    );

    @Test
    @DisplayName("storage.type이 s3이면 S3 저장소만 등록된다")
    void registersS3StorageWhenTypeIsS3() {
        contextRunner
                .withPropertyValues(
                        "discodeit.storage.type=s3"
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
    @DisplayName("storage.type이 local이면 로컬 저장소만 등록된다")
    void registersLocalStorageWhenTypeIsLocal() {
        contextRunner
                .withPropertyValues(
                        "discodeit.storage.type=local",
                        "discodeit.storage.local.root-path=build/test-storage"
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
    @DisplayName("storage.type을 생략하면 로컬 저장소가 기본으로 등록된다")
    void registersLocalStorageByDefault() {
        contextRunner
                .withPropertyValues(
                        "discodeit.storage.local.root-path=build/test-storage-default"
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