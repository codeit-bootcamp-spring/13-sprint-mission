package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;



import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class FileUserService extends FileBase implements UserService {


    public FileUserService(Path path) {
        super(path);
    }

    @Override
    public void createUser(String name, String id, String pw){
        HashMap<UUID,User> userList = this.load();


        // create user
        User user = new User(name, id, pw);
        for (int i = 0; i < 3; i ++){
            if (userList.get(user.getId()) != null) break;
            user = new User(name, id, pw);
        }
        userList.put(user.getId(), user);


        this.save(userList);

    }

    @Override
    public ArrayList<User> getUserById(UUID id){
        HashMap<UUID,User> userList = this.load();
        ArrayList<User> users = new ArrayList<>();
        users.add(userList.get(id));
        return users;
    }

    @Override
    public ArrayList<User> getUserList(){
        HashMap<UUID,User> userList = this.load();
        return new ArrayList<>(userList.values());
    }


    @Override
    public void updateUserInfo(UUID id, String name, String userID, String pw){
        HashMap<UUID,User> userList = this.load();
        User trg = userList.get(id);
        trg.setName(name);
        trg.setUserId(userID);
        trg.setUserPw(pw);
        trg.setUpdatedAt(System.currentTimeMillis());
        this.save(userList);
    }

    @Override
    public void deleteUser(UUID id){
        HashMap<UUID,User> userList = this.load();
        User user = userList.remove(id);
    }

}
