package com.sprint.mission.discodeit.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

public class JCFUserService implements UserService {

    // Map 사용이유 : UUID 를 키로 사용하고 벨류는 실 객체를 저장하는 형태가 좋아서...
    private final Map<UUID, User> data;

    // 생성자
    public JCFUserService() {
        this.data = new HashMap<>();
    }

    @Override
    public User create(User user) {
        data.put(user.getId(), user);
        return user;
    }

    @Override
    public User findById(UUID id) {
        return data.get(id);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public User update(User user) {
        if (!data.containsKey(user.getId())) {
            // return null;
            throw new IllegalArgumentException("존재하지 않는 유저 입니다!");
        }
        data.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}
