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
    public User createOne(String username, String email, Long createdAt) throws IOException {
        User user=new User(username, email, createdAt);

        return userRepository.createOne(user);
    }

    @Override
    public Optional<User> readOne(UUID id) throws IOException {
        return userRepository.readOne(id);
    }

    @Override
    public List<User> readAll() throws IOException {

        return userRepository.readAll();
    }


    @Override
    public User editOne(UUID id, String newUserName, String newEmail, Long updatedAtList) throws IOException {

        User user=userRepository.readOne(id)
                .orElseThrow();
        user.updateUser(newUserName, newEmail, updatedAtList);
        userRepository.createOne(user);
        return user;

    }

    @Override
    public void deleteOne(UUID id) throws IOException {
        userRepository.deleteOne(id);


    }
}
