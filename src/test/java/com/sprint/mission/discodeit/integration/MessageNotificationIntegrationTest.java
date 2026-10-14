package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.config.AdminInitializer;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.kafka.NotificationRequiredTopicListener;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
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

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("메시지 알림 생성 통합 테스트")
class MessageNotificationIntegrationTest {

    @Autowired
    private NotificationRequiredTopicListener notificationRequiredTopicListener;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private ReadStatusRepository readStatusRepository;

    @Autowired
    private MessageRepository messageRepository;

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
        messageRepository.deleteAll();
        readStatusRepository.deleteAll();
        channelRepository.deleteAll();
        userRepository.deleteAll();

        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);

            if (cache != null) {
                cache.clear();
            }
        });
    }

    @Test
    @DisplayName("PRIVATE 채널에서 메시지를 보내면 발신자를 제외한 알림 허용 사용자에게 알림을 생성")
    void messageCreated_notification_success() throws Exception {
        // given
        User author = createUser(
                "user1",
                "user1@test.com",
                "password1"
        );

        User receiver = createUser(
                "user2",
                "user2@test.com",
                "password2"
        );

        Channel channel = channelRepository.save(
                new Channel(
                        ChannelType.PRIVATE,
                        "private-channel",
                        "테스트 채널"
                )
        );

        ReadStatus authorReadStatus = new ReadStatus(
                author,
                channel
        );

        ReadStatus receiverReadStatus = new ReadStatus(
                receiver,
                channel
        );

        readStatusRepository.saveAll(
                List.of(
                        authorReadStatus,
                        receiverReadStatus
                )
        );

        Message message = messageRepository.save(
                new Message(
                        "안녕하세요.",
                        channel,
                        author,
                        List.of()
                )
        );

        MessageCreatedEvent event =
                new MessageCreatedEvent(message.getId());

        String kafkaEvent =
                objectMapper.writeValueAsString(event);

        // when
        notificationRequiredTopicListener
                .onMessageCreatedEvent(kafkaEvent);

        // then
        List<Notification> notifications =
                notificationRepository.findAll();

        assertThat(notifications)
                .hasSize(1);

        Notification notification =
                notifications.get(0);

        assertThat(notification.getReceiver().getId())
                .isEqualTo(receiver.getId());

        assertThat(notification.getReceiver().getId())
                .isNotEqualTo(author.getId());

        assertThat(notification.getTitle())
                .isEqualTo(
                        author.getUsername()
                                + " (#"
                                + channel.getName()
                                + ")"
                );

        assertThat(notification.getContent())
                .isEqualTo(message.getContent());
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