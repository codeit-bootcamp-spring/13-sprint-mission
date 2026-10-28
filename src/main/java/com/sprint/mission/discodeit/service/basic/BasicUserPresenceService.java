package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.event.UserChangedEvent;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserPresenceService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicUserPresenceService implements UserPresenceService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;
  private final UserMapper userMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  @CacheEvict(
      cacheNames = {"users", "channelsByUser"},
      allEntries = true
  )
  public UserDto update(UUID userId, boolean online) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(
            Map.of("userId", userId)
        ));

    UserStatus status = userStatusRepository.findByUserId(userId)
        .orElseGet(() -> new UserStatus(user));

    boolean changed = status.isOnline() != online;

    status.updateOnline(online);
    userStatusRepository.save(status);

    UserDto original = userMapper.toDto(user);

    UserDto userDto = new UserDto(
        original.id(),
        original.username(),
        original.email(),
        original.profile(),
        online,
        original.role()
    );

    if (changed) {
      eventPublisher.publishEvent(
          new UserChangedEvent("users.updated", userDto)
      );
    }

    return userDto;
  }
}