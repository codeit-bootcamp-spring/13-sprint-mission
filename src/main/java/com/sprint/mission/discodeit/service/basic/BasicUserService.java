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

//    public BasicUserService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }

    @Override
    public User create(CreateUserRequest userRequest, Optional<CreateProfileImageRequest> profileImageRequest) {
        // username 중복 검사
        boolean existsUsername = userRepository.findAll()
                .stream()
                .anyMatch(user -> user.getUsername().equals(userRequest.username()));

        if (existsUsername) {
            throw new IllegalArgumentException("이미 존재하는 username입니다.");
        }

        // email 중복 검사
        boolean existsEmail = userRepository.findAll()
                .stream()
                .anyMatch(user -> user.getEmail().equals(userRequest.email()));

        if (existsEmail) {
            throw new IllegalArgumentException("이미 존재하는 email입니다.");
        }

        User user = userRequest.toEntity();
        userRepository.save(user);

        if (profileImageRequest.isPresent()) {
            CreateProfileImageRequest imageRequest = profileImageRequest.get();

            BinaryContent profileImage = new BinaryContent(
                            user.getId(),
                            null,
                            imageRequest.filename(),
                            imageRequest.contentType(),
                            imageRequest.bytes()
            );

//            binaryContentRepository.save(profileImage);
        }

        // UserStatus 같이 생성
        UserStatus status = new UserStatus(UUID.randomUUID(), user.getId());
//        userStatusRepository.save(status);

        return user;
    }

    @Override
    public User find(UUID id) {
        User user = userRepository.findById(id);

        UserStatus status = userStatusRepository.findById(id);

        return UserResponse.from(user, status);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User update(UpdateUserRequest request,
                       Optional<CreateProfileImageRequest> profileImageRequest) {
        User user = userRepository.findById(request.id());

        user.update(request.username(), request.email(), request.password());
        userRepository.save(user);

        if (profileImageRequest.isPresent()) {
            CreateProfileImageRequest image = profileImageRequest.get();

            // 기존 이미지 삭제 또는 조회
            BinaryContent profileImage = new BinaryContent(
                            user.getId(),
                            null,
                            image.filename(),
                            image.contentType(),
                            image.bytes()
            );
            binaryContentRepository.save(profileImage);
        }

        return user;
    }

    @Override
    public void delete(UUID id) {
        // 프로필 이미지 삭제
        BinaryContent profile = binaryContentRepository.findByUserId(id);

        if (profile != null) {
            binaryContentRepository.delete(profile.getId());
        }

        // 상태 정보 삭제
        UserStatus status = userStatusRepository.findByUserId(id);

        if (status != null) {
            userStatusRepository.delete(status.getId());
        }

        // 사용자 삭제
        userRepository.delete(id);
    }

}
