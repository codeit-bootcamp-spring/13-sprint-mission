package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.CreateUserRequest;
import com.sprint.mission.discodeit.dto.UpdateUserRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User create(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 사용자 이름입니다: " + request.getUsername()
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 이메일입니다: " + request.getEmail()
            );
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                null
        );

        return userRepository.save(user);
    }

    @Override
    public User find(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + id
                        )
                );
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User update(
            UUID id,
            UpdateUserRequest request
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + id
                        )
                );

        if (!user.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 사용자 이름입니다: "
                            + request.getUsername()
            );
        }

        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 이메일입니다: "
                            + request.getEmail()
            );
        }

        user.update(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                user.getProfile()
        );

        return user;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + id
                        )
                );

        userRepository.delete(user);
    }
}