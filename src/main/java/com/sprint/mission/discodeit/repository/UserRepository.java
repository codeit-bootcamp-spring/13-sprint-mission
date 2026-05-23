package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

//    void saveToBinary();
//    void loadFromBinary();

    void save();
    void createUser(User user);
    Optional<User> findUser(User user);
    List<User> findAll();
//    void updateUser(User user);
    void deleteUser(User user);




}
