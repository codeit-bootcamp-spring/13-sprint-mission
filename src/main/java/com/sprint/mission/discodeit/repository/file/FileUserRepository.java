package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFSelectFilter;
import com.sprint.mission.discodeit.service.JCFException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class FileUserRepository extends FileBaseRepository implements UserRepository {

    public FileUserRepository(Path path) {
        super(path);
    }

    @Override
    public void create(String name, String id, String pw){
        HashMap<UUID, User> userList = this.load();
        for (int i = 0; i < 3; i ++){
            User user = new User(name, id, pw);
            if (!userList.containsKey(user.getId())) {
                userList.put(user.getId(), user);
                break;
            }
        }
        this.save(userList);
    }

    @Override
    public ArrayList<User> select(JCFSelectFilter fn){
        HashMap<UUID,User> userList = this.load();
        return new ArrayList<>(userList.values().stream()
                .filter(fn::filter)
                .toList());
    }


    @Override
    public void update(UUID id, String name, String userID, String pw){
        HashMap<UUID,User> userList = this.load();
        User user = userList.remove(id);

        user.setName(name);
        user.setUserId(userID);
        user.setUserPw(pw);
        user.setUpdatedAt(System.currentTimeMillis());

        this.save(userList);
    }

    @Override
    public void delete(UUID id){
        HashMap<UUID,User> userList = this.load();
        User user = userList.remove(id);
        if ( user == null) JCFException.throwRuntimeError("User dos not exist.");
    }


}
