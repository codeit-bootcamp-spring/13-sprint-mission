package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

public class JCFUserService implements UserService {

    private final List<User> data;

    public JCFUserService(){
        this.data = new ArrayList<>();
    }

    // 생성
    @Override
    public void createUser(User user) {
        data.add(user);
    }

    // 조회
    @Override
    public User findUser(UUID id) {
        for (User user : data) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        throw new IllegalArgumentException("유저를 찾을 수 없습니다.");
    }

    // 모두 조회
    @Override
    public List<User> findAllUsers() {
        if (!data.isEmpty()) {
            return data;
        }
        return Collections.emptyList();
    }

    // 수정
    @Override
    public void updateUser(UUID id, String username, String email, String password) {
        for (User user : data) {
            if (user.getId().equals(id)) {
                user.update(username, email, password);
                return;
            }
        }

        throw new IllegalArgumentException("유저를 찾을 수 없습니다.");
    }

    // 삭제
    public void deleteUser(UUID id) {
        for (User user : data) {
            if (user.getId().equals(id)) {
                data.remove(user);
                return;
            }
        }
        throw new IllegalArgumentException("유저를 찾을 수 없습니다.");
    }



}