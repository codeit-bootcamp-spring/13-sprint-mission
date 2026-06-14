package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;

    @Override
    public User create(String userName, String email, String pw) {
        User user = new User(userName, email, pw);
        return userRepository.create(user);
    }

    @Override
    public User read(UUID id) {
        User user = userRepository.read(id);
        if(user==null){
            throw new IllegalArgumentException("존재하지 않는 계정입니다");
        }
        return user;
    }

    @Override
    public List<User> readAll() {
        return userRepository.readAll();
    }

    @Override
    public void update(UUID id, String userName, String email, String pw) {
        User user = userRepository.read(id);
        if(user==null) {
            throw new IllegalArgumentException("존재하지 않는 계정입니다");
        }
        user.update(userName, email, pw);
        userRepository.update(user);
    }

    @Override
    public void delete(UUID id) {
        userRepository.delete(id);
    }
}
