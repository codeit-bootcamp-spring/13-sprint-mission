package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;

import java.util.*;

public class JCFUserStatusRepository implements UserStatusRepository {

  private final Map<UUID, UserStatus> database = new HashMap<>();

  @Override
  public UserStatus save(UserStatus userStatus) {
    database.put(userStatus.getId(), userStatus);
    return userStatus;
  }

  @Override
  public Optional<UserStatus> findByUserId(UUID userId) {
    return database.values().stream()
        .filter(us -> us.getUser() != null && us.getUser().getId().equals(userId))
        .findFirst();
  }

  @Override
  public List<UserStatus> findAll() {
    return new ArrayList<>(database.values());
  }

  @Override
  public void delete(UUID id) {
    Optional<UserStatus> targetOptional = findByUserId(id);

    targetOptional.ifPresent(target -> {
      database.remove(target.getId());
    });


  }
}
