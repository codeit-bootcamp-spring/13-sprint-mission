package com.sprint.mission.discodeit.integration;

import com.sprint.mission.discodeit.config.AdminInitializer;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Nested
    @DisplayName("알림 목록 조회 API")
    class GetNotifications {

        @Test
        @DisplayName("인증된 사용자는 자신의 알림 목록을 조회하고 200을 반환")
        void get_success() throws Exception {
            // given
            User receiver = createUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            notificationRepository.save(
                    new Notification(
                            receiver,
                            "테스트 알림",
                            "테스트 내용"
                    )
            );

            DiscodeitUserDetails principal = createPrincipal(receiver);

            // when & then
            mockMvc.perform(
                            get("/api/notifications")
                                    .with(user(principal))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].title")
                            .value("테스트 알림"))
                    .andExpect(jsonPath("$[0].content")
                            .value("테스트 내용"));
        }

        @Test
        @DisplayName("인증되지 않은 사용자가 알림 목록을 조회하면 401을 반환")
        void get_fail_unauthenticated() throws Exception {
            // when & then
            mockMvc.perform(
                            get("/api/notifications")
                    )
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("알림 삭제 API")
    class DeleteNotification {

        @Test
        @DisplayName("본인 알림을 삭제하면 204를 반환")
        void delete_success() throws Exception {
            // given
            User receiver = createUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            Notification notification = notificationRepository.save(
                    new Notification(
                            receiver,
                            "테스트 알림",
                            "테스트 내용"
                    )
            );

            DiscodeitUserDetails principal = createPrincipal(receiver);

            // when & then
            mockMvc.perform(
                            delete(
                                    "/api/notifications/{notificationId}",
                                    notification.getId()
                            )
                                    .with(user(principal))
                                    .with(csrf())
                    )
                    .andExpect(status().isNoContent());

            assertThat(notificationRepository.findById(notification.getId()))
                    .isEmpty();
        }

        @Test
        @DisplayName("다른 사용자의 알림을 삭제하면 403을 반환")
        void delete_fail_not_owner() throws Exception {
            // given
            User receiver = createUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            User otherUser = createUser(
                    "user2",
                    "user2@test.com",
                    "password2"
            );

            Notification notification = notificationRepository.save(
                    new Notification(
                            receiver,
                            "테스트 알림",
                            "테스트 내용"
                    )
            );

            DiscodeitUserDetails principal = createPrincipal(otherUser);

            // when & then
            mockMvc.perform(
                            delete(
                                    "/api/notifications/{notificationId}",
                                    notification.getId()
                            )
                                    .with(user(principal))
                                    .with(csrf())
                    )
                    .andExpect(status().isForbidden());

            assertThat(notificationRepository.findById(notification.getId()))
                    .isPresent();
        }

        @Test
        @DisplayName("존재하지 않는 알림을 삭제하면 404를 반환")
        void delete_fail_not_found() throws Exception {
            // given
            User receiver = createUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            UUID unknownNotificationId = UUID.randomUUID();

            DiscodeitUserDetails principal = createPrincipal(receiver);

            // when & then
            mockMvc.perform(
                            delete(
                                    "/api/notifications/{notificationId}",
                                    unknownNotificationId
                            )
                                    .with(user(principal))
                                    .with(csrf())
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code")
                            .value("NOTIFICATION_NOT_FOUND"));
        }
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

    private DiscodeitUserDetails createPrincipal(User user) {
        UserDto userDto = new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null,
                true,
                user.getRole()
        );

        return new DiscodeitUserDetails(
                userDto,
                user.getPassword()
        );
    }
}