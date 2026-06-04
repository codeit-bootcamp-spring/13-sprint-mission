package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.*;
import org.springframework.stereotype.*;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository repository;
    
    @Override
    public User create(String userName, String email, String passWord) {
        User user = new User(userName, email, passWord);

        repository.create(user);

        return user;
    }

    @Override
    public User read(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID를 찾을 수가 없습니다.");
        }

        User user = repository.read(id);

        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저 ID입니다.");
        }

        return user;
    }

    @Override
    public List<User> readAll() {
        return repository.readAll();
    }

    @Override
    public User update(
            UUID id,
            String userName,
            String email,
            String passWord
    ) {

        User user = repository.read(id);

        user.updateUserName(userName);
        user.updateEmail(email);
        user.updatePassWord(passWord);

        repository.update(id, user);

        return user;
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }

        if (!repository.exists(id)) {
            throw new IllegalArgumentException("존재하지 않는 유저 ID입니다.");
        }

        repository.delete(id);
    }
}