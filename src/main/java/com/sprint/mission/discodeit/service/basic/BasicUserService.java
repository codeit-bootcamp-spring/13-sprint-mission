package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserMapper userMapper;
    private final BinaryContentStorage binaryContentStorage;

    @Override
    @Transactional
    public UserDto create(UserCreateRequest request) {
        if(userRepository.findByUsername(request.userName()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이름입니다.");
        }
        if(userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 계정입니다.");
        }

        BinaryContent savedProfile = profileCheck(request.profile());

        User user = new User(request.userName(), request.email(), request.password(), savedProfile);
        userRepository.save(user);

        UserStatus userStatus = new UserStatus(user);
        userStatusRepository.save(userStatus);

        log.info("사용자 생성 완료: id={}, username={}", user.getId(), user.getUsername());
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> findAll() {
        List<User> users = userRepository.findAll();
        List<UserDto> userDtos = new ArrayList<>();

        for (User user : users) {
            userDtos.add(userMapper.toDto(user));
        }

        return userDtos;
    }

    @Override
    @Transactional
    public UserDto update(UserUpdateRequest request) {
        User user = userRepository.findById(request.id())
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));
        UserStatus userStatus = user.getStatus();

        if (userStatus==null) {
            throw new IllegalArgumentException("존재하지 않는 계정입니다.");
        }

        BinaryContent finalProfileId = user.getProfile();
        if (request.profile() != null) {
            if (finalProfileId != null) {
                binaryContentRepository.delete(user.getProfile());
            }

            finalProfileId = profileCheck(request.profile());
        }

        user.update(request.userName(), request.email(), request.password(), finalProfileId);
        userRepository.save(user);

        log.info("사용자 수정 완료: id={}", user.getId());
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));

        if (user.getProfile() != null) {
            binaryContentRepository.delete(user.getProfile());
        }
        userRepository.delete(user);
        log.info("사용자 삭제 완료: id={}", id);
    }

    private BinaryContent profileCheck(BinaryContentCreateRequest profile) {
        BinaryContent binaryContent = null;
        if (profile != null) {
            binaryContent = new BinaryContent(profile.fileName(), profile.contentType(), profile.size());
            binaryContentStorage.put(binaryContent.getId(), profile.bytes());
            binaryContentRepository.save(binaryContent);
        }
        return binaryContent;
    }
}
