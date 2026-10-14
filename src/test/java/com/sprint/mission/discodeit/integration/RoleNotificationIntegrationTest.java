package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.config.AdminInitializer;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.kafka.NotificationRequiredTopicListener;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("권한 변경 알림 통합 테스트")
class RoleNotificationIntegrationTest {

    @Autowired
    private NotificationRequiredTopicListener notificationRequiredTopicListener;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private AdminInitializer adminInitializer;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        userRepository.deleteAll();

        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);

            if (cache != null) {
                cache.clear();
            }
        });
    }

    @Test
    @DisplayName("사용자 권한이 변경되면 해당 사용자에게 알림을 생성")
    void roleUpdated_notification_success() throws Exception {
        // given
        User user = createUser(
                "user1",
                "user1@test.com",
                "password1"
        );

        RoleUpdatedEvent event = new RoleUpdatedEvent(
                user.getId(),
                Role.USER,
                Role.CHANNEL_MANAGER
        );

        String kafkaEvent =
                objectMapper.writeValueAsString(event);

        // when
        notificationRequiredTopicListener
                .onRoleUpdatedEvent(kafkaEvent);

        // then
        List<Notification> notifications =
                notificationRepository.findAll();

        assertThat(notifications)
                .hasSize(1);

        Notification notification =
                notifications.get(0);

        assertThat(notification.getReceiver().getId())
                .isEqualTo(user.getId());

        assertThat(notification.getTitle())
                .isNotBlank();

        assertThat(notification.getContent())
                .contains("USER")
                .contains("CHANNEL_MANAGER");
    }

    private User createUser(
            String username,
            String email,
            String password
    ) {
        User user = new User(
                username,
                email,
                passwordEncoder.encode(password),
                null,
                Role.USER
        );

        return userRepository.save(user);
    }
}