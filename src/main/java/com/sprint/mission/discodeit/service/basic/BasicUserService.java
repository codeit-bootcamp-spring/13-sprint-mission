package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
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
    public UserDto createUser(UserCreateRequest userCreateRequest,
                              BinaryContentCreateRequest profileRequest) {

        //userName 중복 검사
        if (userRepository.existsByUsername(userCreateRequest.username())){
            throw new IllegalArgumentException("이미 사용중인 이름 입니다.");
        }

        //email 중복 체크
        if (userRepository.existsByEmail(userCreateRequest.email())) {
            throw new IllegalArgumentException("이미 사용중인 이메일 입니다.");
        }

        //프로필 이미지 처리
        BinaryContent profile = null;
        if (profileRequest != null) {
             profile = new BinaryContent(profileRequest.fileName(),
                    profileRequest.fileSize(), profileRequest.contentType());
            binaryContentRepository.save(profile);
            binaryContentStorage.put(profile.getId(), profileRequest.bytes());
        }
        User user = new User(userCreateRequest.username(), userCreateRequest.email(), userCreateRequest.password(), profile, null);
        userRepository.save(user);

        UserStatus userStatus = new UserStatus(user);
        userStatusRepository.save(userStatus);
        log.info("유저 생성 완료 - name: {}, userId: {}", userCreateRequest.username(),  user.getId());
        return  userMapper.toDto(user, userStatus);

    }

    @Transactional(readOnly = true)
    @Override
    public UserDto findByUserId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자 입니다."));

        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElse(null);

        log.info("유저 조회 - name: {}", user.getUsername());

        return userMapper.toDto(user, userStatus);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> findAllUser() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return  new ArrayList<>();
        }
        log.info("전체 유저 조회 완료 - 총 {}명", users.size());
        return  users.stream()
                .map(user -> {
                    UserStatus userStatus = userStatusRepository.findByUserId(user.getId())
                            .orElse(null);
                    return userMapper.toDto(user, userStatus);
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserDto updateUser(UUID userId, UserUpdateRequest userUpdateRequest, BinaryContentCreateRequest profileRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자입니다."));

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
        if (userUpdateRequest.newUsername() != null) {
            if (userRepository.existsByUsername(userUpdateRequest.newUsername())){
                throw new IllegalArgumentException("이미 사용중인 이름입니다.");
            }
            user.updateUserName(userUpdateRequest.newUsername());
        }

        if (userUpdateRequest.newEmail() != null) {
            if (userRepository.existsByEmail(userUpdateRequest.newEmail())) {
                throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
            }
            user.updateUserEmail(userUpdateRequest.newEmail());
        }

        if (userUpdateRequest.newPassword() != null)
            user.updateUserPassword(userUpdateRequest.newPassword());
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
                .orElseThrow(()->new NoSuchElementException("존재하지 않는 사용자 입니다."));
        if (user.getProfile() != null){
            binaryContentRepository.delete(user.getProfile());
        }
        readStatusRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
        log.info("유저 삭제 - name: {}, userId: {}", user.getUsername(), user.getId());
    }
}
