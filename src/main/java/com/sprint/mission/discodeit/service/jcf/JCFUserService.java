package com.sprint.mission.discodeit.service.jcf;


import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.JCFException;
import com.sprint.mission.discodeit.service.UserService;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.UUID;

public class JCFUserService implements UserService {
    private final HashMap<UUID, User> data;
    // private final HashTable<UUID,UUID> user

    public JCFUserService() {
        data = new HashMap<>();
    }

    @Override
    public void createUser(String name, String id, String pw){
        User user = new User(name, id, pw);
        for (int i = 0; i < 3; i ++){
            if (this.data.get(user.getId()) != null) break;
            user = new User(name, id, pw);
        }
        data.put(user.getId(), user);
    }
    @Override
    public ArrayList<User> readUser(UUID id){
        if (!this.data.containsKey(id)) JCFException.throwRuntimeError("");
        ArrayList<User> user = new ArrayList<>();
        user.add(data.get(id));
        return user;
    }
    @Override
    public ArrayList<User> readUserAll(){
        // need to convert - data.values <Collection> - can`t UpShifting
        ArrayList<User> res = new ArrayList<>(data.values());
        return res;
    }
    @Override
    public ArrayList<User> readUserByUserId(String userId){
        ArrayList<User> res = (ArrayList<User>) data.values().stream()
                .filter(user -> user.getUserId().equals(userId))
                .toList();
        return res;
    }

    @Override
    public void updateUser(UUID id, String name, String userID, String pw){
        User trg = data.get(id);
        trg.setName(name);
        trg.setUserId(userID);
        trg.setUserPw(pw);
        trg.setUpdatedAt(System.currentTimeMillis());
    }
    @Override
    public void deleteUser(UUID id){
        User user = data.remove(id);
        if ( user == null) JCFException.throwRuntimeError("User dos not exist.");
    }
}
