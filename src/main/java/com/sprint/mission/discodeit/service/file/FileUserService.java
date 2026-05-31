package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileUserService implements UserService {
    private final UserRepository userRepository;

    public FileUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(String userName, String pw, String email) {
        User user = new User(userName, pw, email);
        userRepository.create(user);
        System.out.println(userName + " 계정이 생성되었습니다!");
        return user;
    }

    @Override
    public User read(UUID id) {
        User user = userRepository.read(id);

        if (user == null) {
            System.out.println("계정이 존재하지 않습니다.");
        }
        return user;
    }

    @Override
    public List<User> readAll() {
        return userRepository.readAll();
    }

    @Override
    public void update(UUID id, String userName, String pw, String email) {
        User user = userRepository.read(id);

        if (user != null) {
            user.update(userName, pw, email);
            userRepository.update(user);
        } else {
            System.out.println("수정할 계정이 존재하지 않습니다.");
        }
    }

    @Override
    public void delete(UUID id) {
        userRepository.delete(id);
    }
}
