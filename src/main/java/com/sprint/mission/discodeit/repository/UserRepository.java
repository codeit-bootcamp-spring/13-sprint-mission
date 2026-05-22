package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

public interface UserRepository {

//    void saveToBinary();
//    void loadFromBinary();

    void saveUser(User user);
    void findUser(User user);
    void findAll();
    void updateUser();
    void deleteUser();




}
