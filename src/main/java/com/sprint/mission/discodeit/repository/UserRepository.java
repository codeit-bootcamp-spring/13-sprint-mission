package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserRepository {
    void create(User user);

    User read(UUID id);

    List<User> readAll();

    void update(User user);

    void delete(UUID id);
}
