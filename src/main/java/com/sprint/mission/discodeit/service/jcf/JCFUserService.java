package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

public class JCFUserService implements UserService {

    private final Map<UUID, User> data;

    public JCFUserService() {
        this.data = new HashMap<>();
    }

    // user 생성 검사 로직
    // null, 공백, 이메일 중복 검사
    @Override
    public User createUser(String name, String email, String password) {
        boolean duplicateCheck = data.values().stream()
                .anyMatch(user -> user.getEmail().equals(email));

            if (name == null || name.isBlank()){
                throw new IllegalArgumentException("이름을 입력해주세요.");
            }
            if (email == null || email.isBlank()){
                throw new IllegalArgumentException("이메일을 입력해주세요.");
            }
            if (password == null || password.isBlank()){
                throw new IllegalArgumentException("비밀번호를 입력해주세요.");
            }
            if (duplicateCheck) {
                    throw new IllegalArgumentException("이미 생성된 이메일 입니다.");
            }
            User user = new User(name, email, password);
            data.put(user.getId(), user);
            return user;
    }

    @Override
    public User findByUser(UUID userId) {
        User user = data.get(userId);
        if (user == null) {
            throw new NoSuchElementException("존재하지 않는 유저입니다.");
        }
        return user;
    }

    @Override
    public List<User> findAllUser() {
        List<User> findAllUser = new ArrayList<>(data.values());
        if (findAllUser.isEmpty()) {
            throw new NoSuchElementException("유저가 존재하지 않습니다.");
        }
        return new ArrayList<>(data.values());
    }

    @Override
    public User updateUser(UUID userId, String name, String email, String password) {
        User user = data.get(userId);
        if (user == null) {
            throw new NoSuchElementException("존재하지 않는 유저입니다.");
        }
        if (name != null){
            user.updateUserName(name);
        }
        if (email != null){
            user.updateUserEmail(email);
        }
        if (password != null){
            user.updateUserPassword(password);
        }
        return user;
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = data.get(userId);
        if (user == null) {
            throw new NoSuchElementException("존재하지 않는 유저입니다.");
        }
        data.remove(userId);
    }
}
