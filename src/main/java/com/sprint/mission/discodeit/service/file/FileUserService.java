package com.sprint.mission.discodeit.service.file;


import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FileUserService implements UserService {


    UserRepository userRepository=new FileUserRepository();

    @Override
    public User createUser(String username, String email, Long createdAt) throws IOException {
        User user=new User(username, email, createdAt);

        return userRepository.saveUser(user);
    }

    @Override
    public Optional<User> readUser(UUID id) throws IOException {
        return userRepository.findUser(id);
    }

    @Override
    public List<User> readUsers() throws IOException {

        return userRepository.findUsers();
    }


    @Override
    public User editUser(UUID id, String newUserName, String newEmail, Long updatedAtList) throws IOException {

        User user=userRepository.findUser(id)
                .orElseThrow();
        user.updateUser(newUserName, newEmail, updatedAtList);
        userRepository.saveUser(user);
        return user;

    }

    @Override
    public void deleteUser(UUID id) throws IOException {
        userRepository.deleteUser(id);


    }
}
