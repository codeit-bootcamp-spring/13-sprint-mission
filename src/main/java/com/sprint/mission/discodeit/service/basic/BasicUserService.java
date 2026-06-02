package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;

    @Override
    public User createUser(String name, String email, String password) {
        boolean duplicateCheck = userRepository.findAll().stream()
                .anyMatch(user -> user.getEmail().equals(email));

        if (name == null || name.isBlank()){
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }
        if (email == null || email.isBlank()){
            throw new IllegalArgumentException("이메일을 입력해주세요");
        }
        if (password == null || password.isBlank()){
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }
        if (duplicateCheck){
            throw new IllegalArgumentException("이미 생성된 이메일 입니다.");
        }
        User user = new User(name, email, password);
        userRepository.save(user);
        return user;
    }

    @Override
    public User findByUser(UUID userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new NoSuchElementException("존재하지 않는 유저 입니다.");
        }
        return user;
    }

    @Override
    public List<User> findAllUser() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new NoSuchElementException("유저가 존재하지 않습니다.");
        }
        return users;
    }

    @Override
    public User updateUser(UUID userId, String name, String email, String password) {
        User user = userRepository.findById(userId);
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
        userRepository.save(user);
        return user;
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new NoSuchElementException("존재 하지 않는 유저 입니다.");
        }
        userRepository.deleteById(userId);
    }
}
