package com.sprint.mission.discodeit.storage.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.sprint.mission.discodeit.config.RetryConfig;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserRole;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@SpringJUnitConfig(classes = {
        RetryConfig.class,
        S3BinaryContentStorageRetryTest.TestConfig.class
})
class S3BinaryContentStorageRetryTest {

    @Autowired
    private BinaryContentStorage storage;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void put_최종_실패하면_세_번_시도하고_관리자에게_알림을_보낸다() {
        // given
        UUID binaryContentId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        String requestId = "request-id-123";

        SdkClientException exception =
                SdkClientException.create(
                        "S3 upload failed"
                );

        User admin = mock(User.class);

        given(admin.getId()).willReturn(adminId);
        given(userRepository.findAllByRole(UserRole.ADMIN))
                .willReturn(List.of(admin));

        given(s3Client.putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        )).willThrow(exception);

        MDC.put("requestId", requestId);

        // when & then
        assertThatThrownBy(
                () -> storage.put(
                        binaryContentId,
                        "test-content".getBytes()
                )
        ).isSameAs(exception);

        then(s3Client).should(times(3))
                .putObject(
                        any(PutObjectRequest.class),
                        any(RequestBody.class)
                );

        then(userRepository).should()
                .findAllByRole(UserRole.ADMIN);

        ArgumentCaptor<String> contentCaptor =
                ArgumentCaptor.forClass(String.class);

        then(notificationService).should()
                .create(
                        eq(adminId),
                        eq("S3 바이너리 데이터 저장 실패"),
                        contentCaptor.capture()
                );

        String notificationContent =
                contentCaptor.getValue();

        assertThat(notificationContent)
                .contains(
                        "Task: S3BinaryContentStorage.put",
                        "RequestId: " + requestId,
                        "BinaryContentId: "
                                + binaryContentId,
                        "Error: S3 upload failed"
                );
    }

    @Configuration
    static class TestConfig {

        @Bean
        S3Client s3Client() {
            return mock(S3Client.class);
        }

        @Bean
        S3Presigner s3Presigner() {
            return mock(S3Presigner.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        NotificationService notificationService() {
            return mock(NotificationService.class);
        }

        @Bean
        BinaryContentStorage binaryContentStorage(
                S3Client s3Client,
                S3Presigner s3Presigner,
                UserRepository userRepository,
                NotificationService notificationService
        ) {
            return new S3BinaryContentStorage(
                    s3Client,
                    s3Presigner,
                    "test-bucket",
                    Duration.ofMinutes(10),
                    userRepository,
                    notificationService
            );
        }
    }
}