package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public UserResponse create(UserRequest dto) {

        if (userRepository.existsByUsername(dto.username())) {
            throw new IllegalArgumentException("이미 사용 중인 이름입니다.");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = new User(dto.username(), dto.email(),  dto.password());

        if (dto.profileImageName() != null && !dto.profileImageName().isEmpty()) {
            BinaryContent profile = new BinaryContent(user.getId(), null, dto.profileImageName());
            binaryContentRepository.save(profile);
            user.updateProfileId(profile);
        }

        userRepository.save(user);

        UserStatus userStatus = new UserStatus(user.getId());
        userStatusRepository.save(userStatus);

        return new UserResponse(user.getId(),user.getUsername(),user.getEmail(),true);
    }

    @Override
    public Optional<UserResponse> findById(UUID id) {
        return userRepository.findById(id)
                .map(user -> new UserResponse
                        (user.getId(),user.getUsername(),user.getEmail(),true));
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(user -> new UserResponse
                (user.getId(), user.getUsername(), user.getEmail(), true))
                .toList();
    }

    @Override
    public UserResponse update(UUID id, UserRequest dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
        user.update(dto.username(), dto.email(), dto.password());
        if (dto.profileImageName() != null && !dto.profileImageName().isEmpty()) {
            BinaryContent newProfile = new BinaryContent(user.getId(), null, dto.profileImageName());
            binaryContentRepository.save(newProfile);
            user.updateProfileId(newProfile);
        }
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), true);
    }

    @Override
    public void delete(UUID id) {

        User user = userRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
        userStatusRepository.deleteById(id);
        if (user.getProfileImageId() != null) {
            binaryContentRepository.deleteById(user.getProfileImageId());
        }

        userRepository.delete(user);
    }

}