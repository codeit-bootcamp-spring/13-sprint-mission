package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.*;
import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.user.*;
import com.sprint.mission.discodeit.mapper.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import com.sprint.mission.discodeit.storage.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;

@Slf4j
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
            throw new UserAlreadyExistsException("username");
        }

        if(command.email() == null || command.email().isBlank()) {
            throw new IllegalArgumentException("이메일은 공백이면 안됩니다.");
        }

        if(repository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException("email");
        }

        if(command.password() == null || command.password().isBlank()) {
            throw new IllegalArgumentException("비밀번호는 공백이면 안됩니다.");
        }

        log.info("사용자 생성 요청");

        User user = new User(
                command.username(),
                command.email(),
                command.password()
        );

        if (profileImage != null) {
            BinaryContent profile = new BinaryContent(
                    profileImage.fileName(),
                    (long)profileImage.bytes().length,
                    profileImage.contentType()
            );
            binaryContentRepository.save(profile);
            binaryContentStorage.put(profile.getId(), profileImage.bytes());
            user.updateProfile(profile);

            log.debug("사용자 프로필 이미지 저장 완료. profileId={}", profile.getId());
        }
        repository.save(user);
        UserStatus userStatus = new UserStatus(user);
        userStatusRepository.save(userStatus);

        log.info("사용자 생성 완료. id={}",
                user.getId());
        return userMapper.toDto(user);
    }


    @Override
    public UserDto findByUserId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

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

        if (command.username() == null || command.username().isBlank()) {
            throw new IllegalArgumentException("사용자 이름은 필수입니다.");
        }

        if (command.email() == null || command.email().isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        if (command.password() == null || command.password().isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }

        log.info("사용자 수정 요청. id ={}", id);

        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        Optional<User> emailOwner = repository.findByEmail(command.email());
        if (emailOwner.isPresent() && !emailOwner.get().getId().equals(id)) {
            throw new UserAlreadyExistsException("email");
        }

        Optional<User> usernameOwner = repository.findByUsername(command.username());
        if (usernameOwner.isPresent() && !usernameOwner.get().getId().equals(id)) {
            throw new UserAlreadyExistsException("username");
        }

        user.updateUserName(command.username());
        user.updateEmail(command.email());
        user.updatePassword(command.password());

        if (profileImage != null) {
            BinaryContent oldProfile = user.getProfile();

            BinaryContentDto profileResponse =
                    binaryContentService.create(profileImage);

            BinaryContent newProfile =
                    binaryContentRepository.findById(profileResponse.id())
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "저장된 프로필 이미지를 찾을 수 없습니다."
                                    )
                            );
            user.updateProfile(newProfile);
            if (oldProfile != null) {
                binaryContentRepository.delete(oldProfile);
            }
        }
        log.info("사용자 수정 완료. id={}", user.getId());

        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void delete(UUID id) {

        if (id == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }

        log.info("사용자 삭제 요청. id={}", id);

        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userStatusRepository.findByUserId(id).ifPresent(userStatusRepository::delete);

        BinaryContent profile = user.getProfile();

        if (profile != null) {
            user.updateProfile(null);
            binaryContentRepository.delete(profile);
        }
        repository.delete(user);
        log.info("사용자 삭제 완료. id={}", id);
    }
}