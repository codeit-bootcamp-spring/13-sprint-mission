package com.sprint.mission.discodeit.service;


import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    void createUser(String name, String id, String pw);
    List<User> readUser(UUID id);
    List<User> readUserAll();
    //List<User> readUserByUserId(String userId); // - id type need to final
    // List<User> readUserPassword(UUID id)
    void updateUser(UUID id, String name, String userID, String pw);
    void deleteUser(UUID id);
}
