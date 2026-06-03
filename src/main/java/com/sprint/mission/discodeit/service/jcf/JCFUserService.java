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
        // 저장 로직
        data.add(user);
    }

    // 조회
    @Override
    public User findUser(UUID id) {
        for (User user : data) {
            if (user.getId().equals(id)) {
                // 저장 로직
                return user;
            }
        }
        throw new IllegalArgumentException("유저를 찾을 수 없습니다.");
    }

    // 모두 조회
    @Override
    public List<User> findAllUsers() {
        if (!data.isEmpty()) {
            // 저장 롲직
            return data;
        }
        return Collections.emptyList();
    }

    // 수정
    // 비즈니스 로직은 사용자 정보를 수정하는 규칙을 처리하는 코드
    @Override
    public void updateUser(UUID id, String username, String email, String password) {
        for (User user : data) {
            // 비즈니스 로직
            if (user.getId().equals(id)) {
                // 비즈니스 로직
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
                // 저장 로직
                data.remove(user);
                return;
            }
        }
        throw new IllegalArgumentException("유저를 찾을 수 없습니다.");
    }



}