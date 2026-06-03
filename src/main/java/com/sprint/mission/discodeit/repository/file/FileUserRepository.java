package com.sprint.mission.discodeit.repository.file;


import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Predicate;

public class FileUserRepository extends FileBaseRepository implements UserRepository {
    private final HashMap<UUID, User> data = new HashMap<>();
    private final Path path;

    private FileUserRepository(Path path) {
        super();
        this.path = path;
        HashMap<UUID, User> fData = load(path);
        for (UUID k : fData.keySet()){
            this.data.put(k, fData.get(k));
        }
    }

    @Override
    public void create(String name, String id, String pw){
        for (int i = 0; i < 3; i ++){
            User user = new User(name, id, pw);
            if (!data.containsKey(user.getId())) {
                data.put(user.getId(), user);
                break;
            }
        }

    }

    @Override
    public ArrayList<User> select(Predicate<User> fn){
        return new ArrayList<>(data.values().stream()
                .filter(fn)
                .toList());
    }


    @Override
    public void update(UUID id, String name, String userID, String pw){
        User user = data.remove(id);

        user.setName(name);
        user.setUserId(userID);
        user.setUserPw(pw);
        user.setUpdatedAt(System.currentTimeMillis());


    }

    @Override
    public void delete(UUID id){
        data.remove(id);
    }

    public static FileUserRepository open(Path path){
        return new FileUserRepository(path);
    }

    public void close(){
        save(data,path);
    }

}
