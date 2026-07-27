package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.*;
import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.mapper.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import com.sprint.mission.discodeit.storage.*;
import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

    private final UserRepository repository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentService binaryContentService;
    private final BinaryContentStorage binaryContentStorage;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto create(CreateUserCommand command, CreateBinaryContentCommand profileImage) {
        if (command == null) {
            throw new IllegalArgumentException("유저 생성 요청은 필수입니다.");
        }

        if(command.username() == null || command.username().isBlank()) {
            throw new IllegalArgumentException("유저의 이름은 공백이면 안됩니다.");
        }

        if (repository.existsByUsername(command.username())) {
            throw new IllegalArgumentException("이미 사용 중인 유저이름입니다.");
        }

        if(command.email() == null || command.email().isBlank()) {
            throw new IllegalArgumentException("이메일은 공백이면 안됩니다.");
        }

        if(repository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if(command.password() == null || command.password().isBlank()) {
            throw new IllegalArgumentException("비밀번호는 공백이면 안됩니다.");
        }

        User user = new User(
                command.username(),
                command.email(),
                command.password()
        );

        BinaryContent profile = user.getProfile();

        if (profileImage != null) {
            profile = new BinaryContent(
                    profileImage.fileName(),
                    (long)profileImage.bytes().length,
                    profileImage.contentType()
            );
            binaryContentRepository.save(profile);
            binaryContentStorage.put(profile.getId(), profileImage.bytes());
            user.updateProfile(profile);
        }
        repository.save(user);
        UserStatus userStatus = new UserStatus(user);
        userStatusRepository.save(userStatus);

        return userMapper.toDto(user);
    }


    @Override
    public UserDto findByUserId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID를 찾을 수가 없습니다.");
        }

        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 ID입니다."));

        return userMapper.toDto(user);

    }

    @Override
    public List<UserDto> findAll() {
        List<User> users = repository.findAll();
        return userMapper.toDtoList(users);
    }

    @Override
    @Transactional
    public UserDto update(UUID id, UpdateUserCommand command,
                          CreateBinaryContentCommand profileImage) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }

        if (command == null) {
            throw new IllegalArgumentException("유저 수정 요청은 필수입니다.");
        }

        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 ID입니다."));

        Optional<User> emailOwner = repository.findByEmail(command.email());
        if (emailOwner.isPresent() && !emailOwner.get().getId().equals(id)) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        Optional<User> usernameOwner = repository.findByUsername(command.username());
        if (usernameOwner.isPresent() && !usernameOwner.get().getId().equals(id)) {
            throw new IllegalArgumentException("이미 사용중인 유저 이름입니다.");
        }

        user.updateUserName(command.username());
        user.updateEmail(command.email());
        user.updatePassword(command.password());

        if (profileImage != null) {
            BinaryContentDto profileResponse = binaryContentService.create(profileImage);
            BinaryContent profile = binaryContentRepository.findById(profileResponse.id())
                    .orElseThrow(() -> new IllegalStateException("저장된 프로필 이미지를 찾을 수 없습니다."));
            user.updateProfile(profile);
        }

        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        System.out.println("delete user id = " + id);

        if (id == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }

        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 ID입니다."));

        userStatusRepository.findByUserId(id).ifPresent(userStatusRepository::delete);

        BinaryContent profile = user.getProfile();

        if (profile != null) {
            user.updateProfile(null);
            binaryContentRepository.delete(profile);
        }
        repository.delete(user);
    }
}