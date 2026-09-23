package com.sprint.mission.discodeit.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(MethodSecurityOwnershipTest.Config.class)
class MethodSecurityOwnershipTest {

  @Configuration
  @EnableMethodSecurity
  @Import({BasicUserService.class, BasicMessageService.class, ResourceOwnership.class})
  static class Config {
  }

  @Autowired
  private UserService userService;

  @Autowired
  private MessageService messageService;

  @MockitoBean
  private UserRepository userRepository;
  @MockitoBean
  private MessageRepository messageRepository;
  @MockitoBean
  private ChannelRepository channelRepository;
  @MockitoBean
  private BinaryContentRepository binaryContentRepository;
  @MockitoBean
  private BinaryContentStorage binaryContentStorage;
  @MockitoBean
  private UserMapper userMapper;
  @MockitoBean
  private MessageMapper messageMapper;
  @MockitoBean
  private PageResponseMapper pageResponseMapper;
  @MockitoBean
  private PasswordEncoder passwordEncoder;

  private UUID currentUserId;

  @BeforeEach
  void authenticate() {
    currentUserId = UUID.randomUUID();
    UserResponse currentUser = new UserResponse(
        currentUserId, "owner", "owner@example.com", null, true, Role.USER);
    DiscodeitUserDetails principal = new DiscodeitUserDetails(currentUser, "password");
    SecurityContextHolder.getContext().setAuthentication(
        UsernamePasswordAuthenticationToken.authenticated(
            principal, null, principal.getAuthorities()));
  }

  @AfterEach
  void clearAuthentication() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void denyOtherUserAccess() {
    UUID otherUserId = UUID.randomUUID();

    assertThatThrownBy(() -> userService.updateUser(otherUserId, null, null))
        .isInstanceOf(AccessDeniedException.class);
    assertThatThrownBy(() -> userService.deleteUser(otherUserId))
        .isInstanceOf(AccessDeniedException.class);

    verify(userRepository, never()).findById(otherUserId);
  }

  @Test
  void denyNonAuthorAccess() {
    UUID messageId = UUID.randomUUID();
    when(messageRepository.existsByIdAndAuthorId(messageId, currentUserId)).thenReturn(false);

    assertThatThrownBy(() -> messageService.updateMessage(messageId, null))
        .isInstanceOf(AccessDeniedException.class);
    assertThatThrownBy(() -> messageService.deleteMessage(messageId))
        .isInstanceOf(AccessDeniedException.class);

    verify(messageRepository, never()).findById(messageId);
  }

  @Test
  void allowOwnerAccess() {
    User user = User.builder()
        .username("owner")
        .email("owner@example.com")
        .password("password")
        .role(Role.USER)
        .build();
    when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));

    UUID messageId = UUID.randomUUID();
    Message message = Message.builder().content("message").attachment(List.of()).build();
    when(messageRepository.existsByIdAndAuthorId(messageId, currentUserId)).thenReturn(true);
    when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

    userService.deleteUser(currentUserId);
    messageService.deleteMessage(messageId);

    verify(userRepository).deleteById(currentUserId);
    verify(messageRepository).deleteById(messageId);
  }
}
