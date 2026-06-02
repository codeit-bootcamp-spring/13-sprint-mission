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
    public User createOne(String username, String email, Long createdAt) throws IOException {
        User user =new User(username, email, createdAt);
        return userRepository.createOne(user);
    }

    @Override
    public Optional<User> readOne(UUID id) throws IOException { // 단건, 다건
        return userRepository.readOne(id);
    }

    @Override
    public List<User> readAll() throws IOException {
        return userRepository.readAll();
    }

    @Override
    public User editOne(UUID id, String newUserName, String newEmail, Long updatedAt) throws IOException {
        User user=userRepository.readOne(id)
                .orElseThrow();
        user.updateUser(newUserName, newEmail, updatedAt);

        return user;
    }

    @Override
    public void deleteOne(UUID id) throws IOException {
        userRepository.deleteOne(id);
    }
}



