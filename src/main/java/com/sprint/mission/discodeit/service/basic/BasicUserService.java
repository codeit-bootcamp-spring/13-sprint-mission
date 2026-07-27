package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.binarycontent.BinaryContentCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserUpdateCommand;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserMapper userMapper;
    private final BinaryContentStorage binaryContentStorage;


    @Override
    public UserDto createUser(UserCreateCommand command,
                              BinaryContentCreateCommand profileRequest) {

        //userName 중복 검사
        if (userRepository.existsByUsername(command.username())){
            log.warn("사용자 생성 실패 - 중복된 username: {}", command.username());
            throw UserAlreadyExistsException.withName(command.username());
        }

        //email 중복 체크
        if (userRepository.existsByEmail(command.email())) {
            log.warn("사용자 생성 실패 - 중복된 email: {}", command.email());
            throw UserAlreadyExistsException.withEmail(command.email());
        }

        //프로필 이미지 처리
        BinaryContent profile = null;
        if (profileRequest != null) {
             profile = new BinaryContent(profileRequest.fileName(),
                    profileRequest.fileSize(), profileRequest.contentType());
            binaryContentRepository.save(profile);
            binaryContentStorage.put(profile.getId(), profileRequest.bytes());
        }
        User user = new User(command.username(), command.email(), command.password(), profile, null);
        userRepository.save(user);

        UserStatus userStatus = new UserStatus(user);
        userStatusRepository.save(userStatus);
        log.info("유저 생성 완료 - name: {}, userId: {}", command.username(),  user.getId());
        return  userMapper.toDto(user, userStatus);

    }

    @Transactional(readOnly = true)
    @Override
    public UserDto findByUserId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.withId(userId));

        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElse(null);

        log.debug("유저 조회 - name: {}", user.getUsername());

        return userMapper.toDto(user, userStatus);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> findAllUser() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return  new ArrayList<>();
        }
        log.debug("전체 유저 조회 완료 - 총 {}명", users.size());
        return  users.stream()
                .map(user -> {
                    UserStatus userStatus = userStatusRepository.findByUserId(user.getId())
                            .orElse(null);
                    return userMapper.toDto(user, userStatus);
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserDto updateUser(UUID userId, UserUpdateCommand command, BinaryContentCreateCommand profileRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.withId(userId));

        //프로필 이미지 선택적 처리
        if (profileRequest != null) {
            if (user.getProfile() != null){
                binaryContentRepository.deleteById(user.getProfile().getId());
            }

            BinaryContent profile = new BinaryContent(
                    profileRequest.fileName(), profileRequest.fileSize(), profileRequest.contentType());
            binaryContentRepository.save(profile);
            binaryContentStorage.put(profile.getId(), profileRequest.bytes());
            user.updateUserProfileId(profile);
        }
        if (command.newUsername() != null) {
            if (userRepository.existsByUsername(command.newUsername())){
                log.warn("이름 변경 실패 - 중복된 username: {}", command.newUsername());
                throw UserAlreadyExistsException.withName(command.newUsername());
            }
            user.updateUserName(command.newUsername());
        }

        if (command.newEmail() != null) {
            if (userRepository.existsByEmail(command.newEmail())) {
                log.warn("이메일 변경 실패 - 중복된 email: {}", command.newEmail());
                throw UserAlreadyExistsException.withEmail(command.newEmail());
            }
            user.updateUserEmail(command.newEmail());
        }

        if (command.newPassword() != null)
            user.updateUserPassword(command.newPassword());
        userRepository.save(user);

        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElse(null);
        if (userStatus == null){
             userStatus = new UserStatus(user);
        }

        log.info("유저 수정 완료 -  name: {}, userId: {}", user.getUsername(), user.getId());

        return userMapper.toDto(user, userStatus);
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->UserNotFoundException.withId(userId));
        if (user.getProfile() != null){
            binaryContentRepository.delete(user.getProfile());
        }
        readStatusRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
        log.info("유저 삭제 - name: {}, userId: {}", user.getUsername(), user.getId());
    }
}
