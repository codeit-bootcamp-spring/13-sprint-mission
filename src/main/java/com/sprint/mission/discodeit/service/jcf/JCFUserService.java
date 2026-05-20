package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.service.*;

import java.util.*;

public class JCFUserService implements UserService {


    private final Map<UUID, User> data;

    public JCFUserService() {
        this.data = new HashMap<>();
    }


    @Override
    public void create(User user) {
        if (user == null) {
            throw new IllegalArgumentException("유저 정보가 없습니다.");
        }
        if (data.containsKey(user.getId())) {
            throw new IllegalArgumentException("이미 존재하고 있는 유저입니다.");
        }

        data.put(user.getId(), user);
    }

    @Override
    public User read(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }
        if (!data.containsKey(id)) {
            throw new IllegalArgumentException("존재하지 않는 ID입니다.");
        }
        return data.get(id);
    }

    @Override
    public List<User> readAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void update(UUID id, User user) {

        if (id == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }

        if (user == null) {
            throw new IllegalArgumentException("수정할 유저 정보가 없습니다.");
        }

        if (!data.containsKey(id)) {
            throw new IllegalArgumentException("수정할 유저가 존재하지 않습니다.");
        }

        data.put(id, user);
}

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }

        if (!data.containsKey(id)) {
            throw new IllegalArgumentException("삭제할 유저가 존재하지 않습니다.");
        }

        data.remove(id);
    }
}

