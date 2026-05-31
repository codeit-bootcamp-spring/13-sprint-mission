package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.Collection;
import java.util.UUID;

public interface UserRepository {
    void save(User user);
    User findById(UUID id);
    Collection<User> findAll();
    void delete(UUID id);
}
