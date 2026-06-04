package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface UserRepository {

    void save(User user);
    List<User> find(Predicate<User> fn);
    void update(UUID id, String name, String pw);
    void delete(UUID id);
}
