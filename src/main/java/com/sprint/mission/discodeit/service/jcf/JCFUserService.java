package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFUserRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.io.IOException;
import java.util.*;

public class JCFUserService implements UserService {


    UserRepository userRepository=new JCFUserRepository();

    @Override
    public User createUser(String username, String email, Long createdAt) throws IOException {
        User user =new User(username, email, createdAt);
        return userRepository.saveUser(user);
    }

    @Override
    public Optional<User> readUser(UUID id) throws IOException { // 단건, 다건
        return userRepository.findUser(id);
    }

    @Override
    public List<User> readUsers() throws IOException {
        return userRepository.findUsers();
    }

    @Override
    public User editUser(UUID id, String newUserName, String newEmail, Long updatedAt) throws IOException {
        User user=userRepository.findUser(id)
                .orElseThrow();
        user.updateUser(newUserName, newEmail, updatedAt);

        return user;
    }

    @Override
    public void deleteUser(UUID id) throws IOException {
        userRepository.deleteUser(id);
    }
}



