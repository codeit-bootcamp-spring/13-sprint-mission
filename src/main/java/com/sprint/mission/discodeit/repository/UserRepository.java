package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    boolean existsUserById(UUID userId);
    boolean existsUserByName(String name);
    boolean existsUserByEmail(String email);
    void createUser(User user);
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserById(UUID userId);
    Optional<User> findUserByNameAndPassword(String username, String password);
    List<User> findAll();
    void save();
    void deleteUser(UUID id);

}
