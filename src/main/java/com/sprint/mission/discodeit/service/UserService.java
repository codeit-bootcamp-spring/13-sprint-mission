package com.sprint.mission.discodeit.service;


import com.sprint.mission.discodeit.entity.User;

import java.util.ArrayList;
import java.util.UUID;

public interface UserService {
    void createUser(String name, String id, String pw);
    ArrayList<User> getUserById(UUID id);
    ArrayList<User> getUserList();
    //List<User> readUserByUserId(String userId); // - id type need to final
    // List<User> readUserPassword(UUID id)
    void updateUserInfo(UUID id, String name, String userID, String pw);
    void deleteUser(UUID id);
}
