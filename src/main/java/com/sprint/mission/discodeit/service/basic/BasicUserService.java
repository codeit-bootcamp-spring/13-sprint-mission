package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final ReadStatusRepository readStatusRepository;

    @Override
    public UserResponse createUser(UserCreateRequest userCreateRequest,
                                   BinaryContentCreateRequest profileRequest) {
        //username 중복체크
        boolean nameDuplicate = userRepository.findAll().stream()
                .anyMatch(user -> userCreateRequest.name().equals(user.getName()));
        if (nameDuplicate) {
            throw new IllegalArgumentException("이미 사용중인 이름 입니다.");
        }

        //email 중복 체크
        boolean emailDuplicate = userRepository.findAll().stream()
                .anyMatch(user -> userCreateRequest.email().equals(user.getEmail()));
        if (emailDuplicate) {
            throw new IllegalArgumentException("이미 사용중인 이메일 입니다.");
        }

        //프로필 이미지 처리
        UUID profileId = null;
        if (profileRequest != null) {
            BinaryContent profile = new BinaryContent(null, profileRequest.fileName(), profileRequest.fileSize(),
                    profileRequest.contentType(),profileRequest.bytes());
            binaryContentRepository.save(profile);
            profileId = profile.getId();
        }
        User user = new User(userCreateRequest.name(), userCreateRequest.email(), userCreateRequest.password());
        userRepository.save(user);

        UserStatus userStatus = new UserStatus(user.getUserId());
        userStatusRepository.save(userStatus);

        return new UserResponse(user.getUserId(), user.getName(), user.getEmail(), profileId, userStatus.isOnline());

    }

    @Override
    public UserResponse findByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자 입니다."));

        UserStatus userStatus = userStatusRepository.findById(userId).get();

        return new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getProfileId(),
                userStatus.isOnline());
    }

    @Override
    public List<UserResponse> findAllUser() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new NoSuchElementException("사용자가 존재하지 않습니다.");
        }

        return  users.stream()
                .map(user -> {
                    UserStatus userStatus = userStatusRepository.findById(user.getUserId()).get();
                    return new UserResponse(
                            user.getUserId(), user.getName(), user.getEmail(), user.getProfileId(), userStatus.isOnline()
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse updateUser(UUID userId, UserUpdateRequest userUpdateRequest, BinaryContentCreateRequest profileRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자입니다."));

        //프로필 이미지 선택적 처리
        if (profileRequest != null) {
            if (user.getProfileId() != null){
                binaryContentRepository.delete(user.getProfileId());
            }
            BinaryContent profile = new BinaryContent(
                    userId, profileRequest.fileName(), profileRequest.fileSize(), profileRequest.contentType(),profileRequest.bytes());
            binaryContentRepository.save(profile);
            user.updateUserProfileId(profile.getId());
        }

        if (userUpdateRequest.name() != null) user.updateUserName(userUpdateRequest.name());
        if (userUpdateRequest.email() != null) user.updateUserEmail(userUpdateRequest.email());
        if (userUpdateRequest.password() != null) user.updateUserPassword(userUpdateRequest.password());
        userRepository.save(user);

        UserStatus userStatus = userStatusRepository.findById(userId).get();
        return new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getProfileId(),
                userStatus.isOnline()
        );
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new NoSuchElementException("존재하지 않는 사용자 입니다."));
        if (user.getProfileId() != null){
            binaryContentRepository.delete(user.getProfileId());
        }
        userStatusRepository.deleteById(user.getUserId());
        readStatusRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }
}
