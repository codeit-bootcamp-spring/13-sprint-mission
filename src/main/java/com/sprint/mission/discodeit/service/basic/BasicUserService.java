package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BasicUserService implements UserService {

    private final UserRepository userRepository;

    public BasicUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(String name, String email, String password) {
        User user = new User(
                name, email, password
        );
        return userRepository.save(user);
    }

    @Override
    public User find(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void update(UUID id, String name, String email, String password) {
        User updatedUser = userRepository.findById(id);
        updatedUser.update(name, email, password);

        userRepository.save(updatedUser);
    }

    @Override
    public void delete(UUID id) {
        userRepository.delete(id);
    }

}
