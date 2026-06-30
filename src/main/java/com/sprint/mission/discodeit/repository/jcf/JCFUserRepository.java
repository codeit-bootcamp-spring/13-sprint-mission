package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.NoArgsConstructor;

import java.util.*;

@NoArgsConstructor
public class JCFUserRepository implements UserRepository {

  private final Map<UUID, User> data = new HashMap<>(); // 인스턴스를 생성할 때 마다 새로운 Map을 만들지 않도록 한다

  @Override
  public User save(User user) {
    data.put(user.getId(), user);
    return user;
  }

  @Override
  public Optional<User> findById(UUID id) {
    return Optional.ofNullable(data.get(id)); // null 반환 또한 저장소의 특성이니 추가해야 함
  }

  @Override
  public List<User> findAll() {
    return data.values().stream().toList();
  }

  @Override
  public void deleteById(UUID id) {
    data.remove(id);
  }

  @Override
  public boolean existById(UUID id) {
    return data.containsKey(id);
  }

  @Override
  public boolean existByEmail(String email) {
    return this.findAll().stream().anyMatch(user -> user.getEmail().equals(email));
  }

  @Override
  public boolean existByUsername(String username) {
    return this.findAll().stream().anyMatch(user -> user.getUsername().equals(username));
  }
}
