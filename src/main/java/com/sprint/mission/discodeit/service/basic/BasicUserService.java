package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.CreateBinaryContentCommand;
import com.sprint.mission.discodeit.dto.command.CreateUserCommand;
import com.sprint.mission.discodeit.dto.command.UpdateUserCommand;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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
    private final PasswordEncoder passwordEncoder;

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

        String encodedPassword = passwordEncoder.encode(command.password());

        User user = new User(
                command.username(),
                command.email(),
                encodedPassword
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
    @Transactional(readOnly = true)
    public UserDto findByUserId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toDto(user);

    }

    @Override
    @Transactional(readOnly = true)
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

        log.info("사용자 수정 요청. id ={}", id);

        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (command.username() != null) {
            if (command.username().isBlank()) {
                throw new IllegalArgumentException("사용자 이름은 공백일 수 없습니다.");
            }

            repository.findByUsername(command.username())
                    .filter(owner -> !owner.getId().equals(id))
                    .ifPresent(owner -> {
                        throw new UserAlreadyExistsException("username");
                    });

            user.updateUserName(command.username());
        }

        if (command.email() != null) {
            if (command.email().isBlank()) {
                throw new IllegalArgumentException("이메일은 공백일 수 없습니다.");
            }

            repository.findByEmail(command.email())
                    .filter(owner -> !owner.getId().equals(id))
                    .ifPresent(owner -> {
                        throw new UserAlreadyExistsException("email");
                    });

            user.updateEmail(command.email());
        }

        if (command.password() != null) {
            if (command.password().isBlank()) {
                throw new IllegalArgumentException("비밀번호는 공백일 수 없습니다.");
            }

            user.updatePassword(command.password());
        }

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