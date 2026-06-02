package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    User create(String username, String email, String password);
    User findById(UUID id);
    List<User> findAll();
    void update(UUID id, String newUsername, String newEmail, String newPassword);
    void delete(UUID id);

}
