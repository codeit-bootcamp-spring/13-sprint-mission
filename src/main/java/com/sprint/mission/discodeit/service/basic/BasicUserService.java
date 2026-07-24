package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.UserService;
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
    private final BinaryContentStorage binaryContentStorage;

    @Override
    @Transactional
    public UserDto create(UserRequest.CreateUserRequest request, CreateBinaryContentRequest profileImage) {
        if (request == null) {
            throw new IllegalArgumentException("유저 생성 요청은 필수입니다.");
        }

        if(request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("유저의 이름은 공백이면 안됩니다.");
        }

        if (repository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("이미 사용 중인 유저이름입니다.");
        }

        if(request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("이메일은 공백이면 안됩니다.");
        }

        if(repository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if(request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("비밀번호는 공백이면 안됩니다.");
        }

        User user = new User(
                request.username(),
                request.email(),
                request.password()
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

        return UserDto.from(user, userStatus, profile);
    }


    @Override
    public UserDto find(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID를 찾을 수가 없습니다.");
        }

        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 ID입니다."));

        UserStatus userStatus = userStatusRepository.findByUserId(id).orElse(null);
        BinaryContent profile = user.getProfile();
        return UserDto.from(user, userStatus, profile);

    }

    @Override
    public List<UserDto> findAll() {
        return repository.findAll().stream()
                .map (user -> {
                UserStatus userStatus = userStatusRepository.findByUserId(user.getId()).orElse(null);
                BinaryContent profile = user.getProfile();
                return UserDto.from(user, userStatus, profile);
        }).toList();
    }

    @Override
    @Transactional
    public UserDto update(UUID id, UserRequest.UpdateUserRequest request,
                          CreateBinaryContentRequest profileImage) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }

        if (request == null) {
            throw new IllegalArgumentException("유저 수정 요청은 필수입니다.");
        }

        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 ID입니다."));

        Optional<User> emailOwner = repository.findByEmail(request.email());
        if (emailOwner.isPresent() && !emailOwner.get().getId().equals(id)) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        Optional<User> usernameOwner = repository.findByUsername(request.username());
        if (usernameOwner.isPresent() && !usernameOwner.get().getId().equals(id)) {
            throw new IllegalArgumentException("이미 사용중인 유저 이름입니다.");
        }

        user.updateUserName(request.username());
        user.updateEmail(request.email());
        user.updatePassword(request.password());

        BinaryContent profile = null;
        if (profileImage != null) {
            profile = new BinaryContent(
                    profileImage.fileName(),
                    (long)profileImage.bytes().length,
                    profileImage.contentType()
            );
            binaryContentRepository.save(profile);
            binaryContentStorage.put(profile.getId(), profileImage.bytes());
            user.updateProfile(profile);
        } else  {
            profile = user.getProfile();
        }

        UserStatus userStatus = userStatusRepository.findByUserId(id).orElse(null);

        return UserDto.from(user, userStatus, profile);
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