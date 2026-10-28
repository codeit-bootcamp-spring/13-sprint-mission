package com.sprint.mission.discodeit.integration;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.dto.command.user.UserRoleUpdateCommand;
import com.sprint.mission.discodeit.events.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentStorage;
import com.sprint.mission.discodeit.service.NotificationService;
import com.sprint.mission.discodeit.service.basic.BinaryContentService;
import com.sprint.mission.discodeit.service.basic.UserRoleManager;
import com.sprint.mission.discodeit.storage.BinaryContentUpload;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.sprint.mission.discodeit.support.AsyncTestSupport.awaitIdle;
import static com.sprint.mission.discodeit.support.NotificationTestFixtures.messageCreatedEvent;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Spring 이벤트·트랜잭션·비동기 실행기를 함께 실행하여 작업 거절과 실패의 영향을 확인한다.
 * 외부 파일 저장소와 알림 서비스는 대체하고, 업무 DB 커밋과 별도 상태 저장의 결과를 검증한다.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:async-events;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "discodeit.storage.type=local",
        "discodeit.async.file-upload.core-pool-size=1", "discodeit.async.file-upload.max-pool-size=1",
        "discodeit.async.file-upload.queue-capacity=0",
        "discodeit.async.notification.core-pool-size=1", "discodeit.async.notification.max-pool-size=1",
        "discodeit.async.notification.queue-capacity=0"
})
@ActiveProfiles("test")
@RecordApplicationEvents
@ExtendWith(OutputCaptureExtension.class)
class AsyncEventExecutionIntegrationTest {
    @Autowired BinaryContentService service;
    @Autowired BinaryContentRepository repository;
    @Autowired ThreadPoolTaskExecutor fileUploadExecutor;
    @Autowired ThreadPoolTaskExecutor notificationExecutor;
    @Autowired PlatformTransactionManager transactions;
    @Autowired ApplicationEventPublisher events;
    @Autowired ApplicationEvents recordedEvents;
    @Autowired UserRepository users;
    @Autowired UserRoleManager roles;
    @MockitoBean BinaryContentStorage storage;
    @MockitoBean NotificationService notifications;

    @AfterEach
    void cleanup() {
        awaitIdle(fileUploadExecutor, notificationExecutor);
        repository.deleteAll();
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectedUploadCommitsFailAndDeletesTemporaryFilesIncludingRollback() throws Exception {
        var release = occupy(fileUploadExecutor);
        try {
            var stream = new TrackingStream();
            UUID id = service.create(file(stream)).orElseThrow().getId();
            assertThat(repository.findById(id).orElseThrow().getStatus()).isEqualTo(BinaryContentStatus.FAIL);
            assertThat(stream.closed).isTrue();
            var rollbackStream = new TrackingStream();
            var tx = new TransactionTemplate(transactions);
            UUID rolledBack = tx.execute(status -> {
                UUID created = service.create(file(rollbackStream)).orElseThrow().getId();
                status.setRollbackOnly();
                return created;
            });
            assertThat(repository.findById(rolledBack)).isEmpty();
            assertThat(rollbackStream.closed).isTrue();
            assertThat(recordedEvents.stream(BinaryContentCreatedEvent.class).toList())
                    .hasSize(2)
                    .allSatisfy(event -> assertThat(event.contentUpload().temporaryFile()).doesNotExist());
            verifyNoInteractions(storage);
        } finally { release.countDown(); }
    }

    @Test
    void interruptedWorkerPersistsFailureInIndependentTransaction() {
        var stream = new TrackingStream();
        given(storage.put(any(), any())).willAnswer(invocation -> {
            invocation.<BinaryContentUpload>getArgument(1).inputStream().close();
            Thread.currentThread().interrupt();
            throw new IllegalStateException("인터럽트된 업로드");
        });
        UUID id = service.create(file(stream)).orElseThrow().getId();
        await().untilAsserted(() -> assertThat(repository.findById(id).orElseThrow().getStatus()).isEqualTo(BinaryContentStatus.FAIL));
        assertThat(stream.closed).isTrue();
        awaitIdle(fileUploadExecutor);
        assertThat(recordedEvents.stream(BinaryContentCreatedEvent.class).findFirst().orElseThrow()
                .contentUpload().temporaryFile()).doesNotExist();
    }

    @Test
    void rejectedNotificationsAreRecordedAndDoNotUndoMainCommit(CapturedOutput output) throws Exception {
        var release = occupy(notificationExecutor);
        UUID userId = users.saveAndFlush(com.sprint.mission.discodeit.support.NotificationTestFixtures.user()).getId();
        try {
            new TransactionTemplate(transactions).executeWithoutResult(status -> {
                events.publishEvent(messageCreatedEvent(UUID.randomUUID(), userId, "general", "author", "hello"));
                roles.updateRole(userId, new UserRoleUpdateCommand(Role.ADMIN));
            });
            verifyNoInteractions(notifications);
            assertThat(users.findById(userId).orElseThrow().getRole()).isEqualTo(Role.ADMIN);
            assertThat(output.getAll()).contains("실행기가 메시지 알림 작업을 거부했습니다.", "실행기가 권한 변경 알림 작업을 거부했습니다.", userId.toString());
        } finally { release.countDown(); users.deleteById(userId); }
    }

    private CountDownLatch occupy(ThreadPoolTaskExecutor executor) throws Exception {
        var started = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        executor.execute(() -> {
            started.countDown();
            try { release.await(10, TimeUnit.SECONDS); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
        return release;
    }

    private MockMultipartFile file(TrackingStream stream) {
        return new MockMultipartFile("file", "file.txt", "text/plain", new byte[]{1}) {
            @Override public InputStream getInputStream() { return stream; }
        };
    }

    private static class TrackingStream extends ByteArrayInputStream {
        volatile boolean closed;
        TrackingStream() { super(new byte[]{1}); }
        @Override public void close() throws IOException { closed = true; super.close(); }
    }
}
