package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.CreateUserRequest;
import com.sprint.mission.discodeit.dto.UpdateUserRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

    private static final Logger log =
            LoggerFactory.getLogger(BasicUserService.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto create(CreateUserRequest request) {
        log.debug(
                "사용자 생성 요청: username={}, email={}",
                request.getUsername(),
                request.getEmail()
        );

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn(
                    "사용자 생성 실패 - 사용자 이름 중복: username={}",
                    request.getUsername()
            );

            throw new UserAlreadyExistsException(
                    "username",
                    request.getUsername()
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn(
                    "사용자 생성 실패 - 이메일 중복: email={}",
                    request.getEmail()
            );

            throw new UserAlreadyExistsException(
                    "email",
                    request.getEmail()
            );
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                null
        );

        User savedUser = userRepository.save(user);

        log.info("사용자 생성 완료: userId={}", savedUser.getId());

        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDto find(UUID id) {
        log.debug("사용자 조회 요청: userId={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("사용자 조회 실패 - 사용자 없음: userId={}", id);
                    return new UserNotFoundException(id);
                });

        log.debug("사용자 조회 완료: userId={}", id);

        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> findAll() {
        log.debug("사용자 목록 조회 요청");

        List<UserDto> users = userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();

        log.debug("사용자 목록 조회 완료: count={}", users.size());

        return users;
    }

    @Override
    @Transactional
    public UserDto update(
            UUID id,
            UpdateUserRequest request
    ) {
        log.debug(
                "사용자 수정 요청: userId={}, username={}, email={}",
                id,
                request.getUsername(),
                request.getEmail()
        );

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("사용자 수정 실패 - 사용자 없음: userId={}", id);
                    return new UserNotFoundException(id);
                });

        boolean usernameChanged =
                !user.getUsername().equals(request.getUsername());

        if (usernameChanged
                && userRepository.existsByUsername(request.getUsername())) {

            log.warn(
                    "사용자 수정 실패 - 사용자 이름 중복: userId={}, username={}",
                    id,
                    request.getUsername()
            );

            throw new UserAlreadyExistsException(
                    "username",
                    request.getUsername()
            );
        }

        boolean emailChanged =
                !user.getEmail().equals(request.getEmail());

        if (emailChanged
                && userRepository.existsByEmail(request.getEmail())) {

            log.warn(
                    "사용자 수정 실패 - 이메일 중복: userId={}, email={}",
                    id,
                    request.getEmail()
            );

            throw new UserAlreadyExistsException(
                    "email",
                    request.getEmail()
            );
        }

        user.update(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                user.getProfile()
        );

        log.info("사용자 수정 완료: userId={}", id);

        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.debug("사용자 삭제 요청: userId={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("사용자 삭제 실패 - 사용자 없음: userId={}", id);
                    return new UserNotFoundException(id);
                });

        userRepository.delete(user);

        log.info("사용자 삭제 완료: userId={}", id);
    }
}