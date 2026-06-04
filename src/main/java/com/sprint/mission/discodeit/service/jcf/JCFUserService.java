package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

public class JCFUserService implements UserService {
    private final Map<UUID, User> data;

    public JCFUserService() {
        this.data = new HashMap<>();
    }

    @Override
    public User create(String userName, String pw, String email) {
        User user = new User(userName, pw, email);
        data.put(user.getId(), user);
        System.out.println(userName + "님의 계정이 생성되었습니다!");
        return user;
    }

    @Override
    public User read(UUID id) {
        if (!data.containsKey(id)) {
            System.out.println("계정이 존재하지 않습니다.");
        }
        return data.get(id);
    }

    @Override
    public List<User> readAll() {
        return data.values().stream()
                            .toList();
    }

    @Override
    public void update(UUID id, String userName, String pw, String email) {
        if(data.containsKey(id)){
            User user = data.get(id);
            user.update(userName, pw, email);
        }
    }

    @Override
    public void delete(UUID id) {
        if(!data.containsKey(id)){
            System.out.println("계정이 존재하지 않습니다.");
        }
        data.remove(id);
    }
}
