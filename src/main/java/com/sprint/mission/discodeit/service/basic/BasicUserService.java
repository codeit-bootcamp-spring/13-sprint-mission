package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.CreateProfileImageRequest;
import com.sprint.mission.discodeit.dto.request.CreateUserRequest;
import com.sprint.mission.discodeit.dto.request.UpdateUserRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public UserResponse create(CreateUserRequest userRequest, Optional<CreateProfileImageRequest> profileImageRequest) {
        // username 중복 검사
        boolean existsUsername = userRepository.findAll()
                .stream()
                .anyMatch(user -> user.getUsername().equals(userRequest.getUsername()));

        if (existsUsername) {
            throw new IllegalArgumentException("이미 존재하는 username입니다.");
        }

        // email 중복 검사
        boolean existsEmail = userRepository.findAll()
                .stream()
                .anyMatch(user -> user.getEmail().equals(userRequest.getEmail()));

        if (existsEmail) {
            throw new IllegalArgumentException("이미 존재하는 email입니다.");
        }

        // password 암호화
        String encodedPassword = PasswordUtil.encode(userRequest.getPassword());

        User user = userRequest.toEntity(encodedPassword);
        userRepository.save(user);

        if (profileImageRequest.isPresent()) {
            CreateProfileImageRequest imageRequest = profileImageRequest.get();

            BinaryContent profileImage = new BinaryContent(
                            user.getId(),
                            null,
                            imageRequest.getFilename(),
                            imageRequest.getContentType(),
                            imageRequest.getBytes()
            );

            binaryContentRepository.save(profileImage);
        }

        // UserStatus 같이 생성
        UserStatus status = new UserStatus(UUID.randomUUID(), user.getId());
        userStatusRepository.save(status);

        return UserResponse.from(
                user,
                status
        );
    }

    @Override
    public UserResponse find(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        UserStatus status = userStatusRepository.findByUserId(id)
                .orElseThrow(() -> new IllegalArgumentException("상태 정보를 찾을 수 없습니다."));

        return UserResponse.from(user, status);
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(user -> {
                    UserStatus status = userStatusRepository.findByUserId(user.getId())
                            .orElseThrow(() -> new IllegalArgumentException("상태 정보를 찾을 수 없습니다."));

                    return UserResponse.from(
                            user,
                            status
                    );
                })
                .toList();
    }

    @Override
    public UserResponse update(UUID id, UpdateUserRequest request,
                       Optional<CreateProfileImageRequest> profileImageRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 암호화
        String encodedPassword = PasswordUtil.encode(request.getPassword());

        user.update(request.getUsername(), request.getEmail(), encodedPassword);
        userRepository.save(user);

        if (profileImageRequest.isPresent()) {
            CreateProfileImageRequest image = profileImageRequest.get();

            // 기존 이미지 삭제 또는 조회
            BinaryContent profileImage = new BinaryContent(
                            user.getId(),
                            null,
                            image.getFilename(),
                            image.getContentType(),
                            image.getBytes()
            );
            binaryContentRepository.save(profileImage);
        }

        UserStatus status = userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("상태 정보를 찾을 수 없습니다."));

        return UserResponse.from(
                user,
                status
        );
    }

    @Override
    public void delete(UUID id) {
        binaryContentRepository.findByUserId(id)
                .forEach(profile ->
                        binaryContentRepository.delete(profile.getId()));

        userStatusRepository.findByUserId(id)
                .ifPresent(status ->
                        userStatusRepository.delete(status.getId()));

        userRepository.delete(id);
    }

}
