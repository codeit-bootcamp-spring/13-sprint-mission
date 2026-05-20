package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.JCFException;
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
    public ArrayList<User> readUser(UUID id){
        HashMap<UUID,User> userList = this.load();
        ArrayList<User> users = new ArrayList<>();
        users.add(userList.get(id));
        return users;
    }

    @Override
    public ArrayList<User> readUserAll(){
        HashMap<UUID,User> userList = this.load();
        return new ArrayList<>(userList.values());
    }

//    @Override
//    public ArrayList<User> readUserByUserId(String userId){
//        HashMap<UUID,User> userList = this.load();
//        ArrayList<User> res = (ArrayList<User>) userList.values().stream()
//                .filter(user -> user.getUserId().equals(userId))
//                .toList();
//        if(res.isEmpty()) JCFException.throwRuntimeError("User dos not exist.");
//        return res;
//    }

    @Override
    public void updateUser(UUID id, String name, String userID, String pw){
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
        if ( user == null) JCFException.throwRuntimeError("User dos not exist.");
    }

}
