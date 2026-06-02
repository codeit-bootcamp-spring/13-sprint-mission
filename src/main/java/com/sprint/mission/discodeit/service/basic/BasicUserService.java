package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class BasicUserService implements UserService {

    private final UserRepository userRepository;

    public BasicUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(String username, String email, String password) {
        User user = new User(username, email, password);
        return userRepository.save(user);
    }

    @Override
    public User findById(UUID userId) {
        User user = userRepository.findById(userId);
        if (user ==null) {
            throw new NoSuchElementException(userId + " 유저를 찾을 수 없습니다.");
        }
        return user;

    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void update(UUID userId, String newUsername, String newEmail, String newPassword) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new NoSuchElementException(userId + " 유저를 찾을 수 없습니다.");
        }
        if (newUsername != null) {
            user.updateProfile(newUsername);
        }
        if (newEmail != null) {
            user.updateEmail(newEmail);
        }
        if (newPassword != null) {
            user.updatePassword(newPassword);
        }
    }

    @Override
    public void delete(UUID userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new NoSuchElementException(userId + " 유저를 찾을 수 없습니다.");
        }
        userRepository.delete(userId);

    }
}
