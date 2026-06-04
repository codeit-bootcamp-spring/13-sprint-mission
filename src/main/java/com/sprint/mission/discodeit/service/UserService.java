package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User create(String userName, String pw, String email);

    User read(UUID id);

    List<User> readAll();

    void update(UUID id, String userName, String pw, String email);

    void delete(UUID id);
}
