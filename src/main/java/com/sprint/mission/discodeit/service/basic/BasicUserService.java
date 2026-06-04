package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;


    @Override
    public User create(String name, String email) {
       User user = new User(name, email);
       userRepository.save(user);
       return user;
    }

    @Override
    public User findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public Collection<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void update(UUID id, String name, String email) {
        User user= userRepository.findById(id);

        if (user != null) {
            user.renew(name, email);
            userRepository.save(user);
        }
    }

    @Override
    public void delete(UUID id) {
        userRepository.delete(id);
    }
}
