package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;
    private final UserMapper userMapper;


    @Override
    @Transactional
    public UserDto create(UserCreateRequest createRequest) {

        validateUniqueUser(
                createRequest.username(),
                createRequest.email());

        BinaryContent profile = null;
        if (createRequest.profileBytes() != null) {
            profile = new BinaryContent(
                    createRequest.profileName(),
                    createRequest.profileContentType(),
                    (long) createRequest.profileBytes().length
            );
            binaryContentRepository.save(profile);
        }

        User user = new User(
                createRequest.username(),
                createRequest.email(),
                createRequest.password(),
                profile
        );

        userRepository.save(user);

        UserStatus userStatus = new UserStatus(user, Instant.now());

        userStatusRepository.save(userStatus);
        return userMapper.toDto(user);
    }

    @Override
    public UserDto findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 유저입니다."));

        return userMapper.toDto(user);
    }

    @Override
    public Collection<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto update ( UUID userId, UserUpdateRequest updateRequest) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 유저입니다."));

        validateUniqueUserForUpdate(
                userId,
                updateRequest.username(),
                updateRequest.email());


        BinaryContent profile = user.getProfile();

        if (updateRequest.profileBytes() != null) {
            profile = new BinaryContent(
                    updateRequest.profileName(),
                    updateRequest.profileContentType(),
                    (long) updateRequest.profileBytes().length
            );
            binaryContentRepository.save(profile);
        }

        user.renew(
                updateRequest.username(),
                updateRequest.email(),
                updateRequest.password(),
                profile);

        userRepository.save(user);
        return userMapper.toDto(user);
        }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 유저입니다."));

        BinaryContent profile = user.getProfile();
        if (profile != null) {
            binaryContentRepository.delete(profile);
        }

        userStatusRepository.findByUserId(id)
                .ifPresent(userStatusRepository::delete);

        userRepository.delete(user);
    }

    private void validateUniqueUser(String username, String email) {
        if (userRepository.findByName(username).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이름입니다.");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    private void validateUniqueUserForUpdate(UUID id, String username, String email) {
        userRepository.findByName(username)
                .filter(user -> !user.getId().equals(id))
                .ifPresent(user -> {
                    throw new IllegalArgumentException("이미 사용 중인 유저이름 입니다.");
                });

        userRepository.findByEmail(email)
                .filter(user -> !user.getId().equals(id))
                .ifPresent(user -> {
                    throw new IllegalArgumentException("이미 사용 중인 E-mail입니다.");
                });
    }

}
