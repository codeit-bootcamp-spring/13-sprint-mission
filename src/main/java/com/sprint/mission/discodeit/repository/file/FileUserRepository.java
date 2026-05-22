package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Predicate;

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
    public ArrayList<User> select(Predicate<User> fn){
        HashMap<UUID,User> userList = this.load();
        return new ArrayList<>(userList.values().stream()
                .filter(fn)
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
    }


}
