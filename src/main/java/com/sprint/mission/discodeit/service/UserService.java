package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    void createUser(User user);

    User findUser(UUID id);

    List<User> findAllUsers();

    void updateUser(UUID id, String name, String email, String password);

    void deleteUser(UUID id);

}
