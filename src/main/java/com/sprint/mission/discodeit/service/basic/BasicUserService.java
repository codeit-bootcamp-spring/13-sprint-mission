package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.util.ArrayList;
import java.util.UUID;

public class BasicUserService implements UserService {
    private final UserRepository fur;

    public BasicUserService(UserRepository usr) {
        fur = usr;
    }

    @Override
    public void createUser(String name, String id, String pw){
        fur.create(name, id, pw);
    }

    @Override
    public ArrayList<User> getUserById(UUID id){
        return fur.select((c) -> c.getId().equals(id));
    }

    @Override
    public ArrayList<User> getUserList(){
        return fur.select(((c) -> true));
    }

    @Override
    public void updateUserInfo(UUID id, String name, String userID, String pw){
        fur.update(id, name, userID, pw);
    }

    @Override
    public void deleteUser(UUID id){
        fur.delete(id);
    }
}
