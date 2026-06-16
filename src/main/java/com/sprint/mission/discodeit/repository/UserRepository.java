package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public interface UserRepository {

    void save(User user);
    List<User> find(Predicate<User> fn);
    List<User> findAll();
    Optional<User> findByID(UUID id);
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
    void delete(UUID id);
}
