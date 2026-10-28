package com.sprint.mission.discodeit.integration;

import com.sprint.mission.discodeit.dto.command.notification.NotificationCreateCommand;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.UUID;

import static com.sprint.mission.discodeit.support.NotificationTestFixtures.messageCreatedEvent;
import static org.assertj.core.api.Assertions.*;

/**
 * 격리한 PostgreSQL에서 실제 마이그레이션과 제약조건을 실행한다.
 * 메시지 생성 이벤트 픽스처는 새 DTO 계약을 따르며 Mock Repository로 DB 결과를 대체하지 않는다.
 */
@DisplayName("알림 PostgreSQL 마이그레이션 테스트")
class NotificationMigrationTest {
    @Test
    void migrateV5_backfillsPreferencesAndEnforcesNotificationConstraints() {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")) {
            postgres.start();
            DriverManagerDataSource dataSource = new DriverManagerDataSource(
                    postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
            Flyway.configure().dataSource(dataSource).target("4").load().migrate();
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            UUID userId = UUID.randomUUID();
            UUID privateId = UUID.randomUUID();
            UUID publicId = UUID.randomUUID();
            jdbc.update("INSERT INTO users (id, username, email, password, role) VALUES (?, 'receiver', 'receiver@test.local', 'password', 'USER')", userId);
            jdbc.update("INSERT INTO channels (id, type) VALUES (?, 'PRIVATE'), (?, 'PUBLIC')", privateId, publicId);
            jdbc.update("INSERT INTO read_statuses (user_id, channel_id, last_read_at) VALUES (?, ?, now()), (?, ?, now())", userId, privateId, userId, publicId);

            Flyway flyway = Flyway.configure().dataSource(dataSource).target("5").load();
            flyway.migrate();
            flyway.validate();

            assertThat(jdbc.queryForObject("SELECT notification_enabled FROM read_statuses WHERE channel_id = ?", Boolean.class, privateId)).isTrue();
            assertThat(jdbc.queryForObject("SELECT notification_enabled FROM read_statuses WHERE channel_id = ?", Boolean.class, publicId)).isFalse();
            assertThat(jdbc.queryForObject("""
                    SELECT data_type FROM information_schema.columns
                    WHERE table_schema = 'public' AND table_name = 'read_statuses' AND column_name = 'notification_enabled'
                    """, String.class)).isEqualTo("boolean");
            assertThat(jdbc.queryForObject("""
                    SELECT column_default FROM information_schema.columns
                    WHERE table_schema = 'public' AND table_name = 'read_statuses' AND column_name = 'notification_enabled'
                    """, String.class)).isNull();
            assertThat(jdbc.queryForObject("""
                    SELECT is_nullable FROM information_schema.columns
                    WHERE table_schema = 'public' AND table_name = 'read_statuses' AND column_name = 'notification_enabled'
                    """, String.class)).isEqualTo("NO");

            jdbc.update("DELETE FROM read_statuses WHERE channel_id = ?", publicId);
            assertThatThrownBy(() -> jdbc.update("INSERT INTO read_statuses (user_id, channel_id, last_read_at) VALUES (?, ?, now())", userId, publicId))
                    .isInstanceOf(DataIntegrityViolationException.class);
            jdbc.update("INSERT INTO read_statuses (user_id, channel_id, last_read_at, notification_enabled) VALUES (?, ?, now(), false)", userId, publicId);

            UUID notificationId = UUID.randomUUID();
            String source = "a".repeat(251) + "😀" + "한".repeat(10);
            NotificationCreateCommand command = new NotificationCreateCommand(userId,
                    messageCreatedEvent(publicId, userId, "general", "sender", source));
            jdbc.update("INSERT INTO notifications (id, receiver_id, title, content) VALUES (?, ?, ?, ?)",
                    notificationId, userId, command.title(), command.content());
            assertThat(jdbc.queryForObject("SELECT content FROM notifications WHERE id = ?", String.class, notificationId))
                    .isEqualTo("a".repeat(251) + "😀...");
            assertThat(jdbc.queryForObject("SELECT char_length(content) FROM notifications WHERE id = ?", Integer.class, notificationId)).isEqualTo(255);
            assertThatThrownBy(() -> jdbc.update("INSERT INTO notifications (receiver_id, title, content) VALUES (?, 'title', 'content')", UUID.randomUUID()))
                    .isInstanceOf(DataIntegrityViolationException.class);
            assertThatThrownBy(() -> jdbc.update("INSERT INTO notifications (receiver_id, title, content) VALUES (?, 'title', NULL)", userId))
                    .isInstanceOf(DataIntegrityViolationException.class);

            jdbc.update("DELETE FROM users WHERE id = ?", userId);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM notifications", Long.class)).isZero();
        }
    }

    @Test
    void emptyDatabase_migratesAllVersionsAndValidates() {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")) {
            postgres.start();
            Flyway flyway = Flyway.configure().dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .target("5").load();
            assertThat(flyway.migrate().migrationsExecuted).isEqualTo(5);
            flyway.validate();
            assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("5");
        }
    }
}
