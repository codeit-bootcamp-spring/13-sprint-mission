package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class JCFUserRepository implements UserRepository {
    private final HashMap<UUID, User> data = new HashMap<UUID, User>();

    JCFUserRepository() {}

    @Override
    public void create(String name, String id, String pw){
        for (int i = 0; i < 3; i ++){
            User user = new User(name, id, pw);
            if (!this.data.containsKey(user.getId())) {
                data.put(user.getId(), user);
                break;
            }
        }
    };

    @Override
    public ArrayList<User> select(JCFSelectFilter fn){
         return new ArrayList<>(data.values().stream()
                .filter(fn::filter)
                .toList());
    };

    @Override
    public void update(UUID id, String name, String userID, String pw){
        User user = data.get(id);
        user.setName(name);
        user.setUserId(userID);
        user.setUserPw(pw);
        user.setUpdatedAt(System.currentTimeMillis());
    };

    @Override
    public void delete(UUID id){
        data.remove(id);
    };
}
