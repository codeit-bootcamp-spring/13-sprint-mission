package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentResponse;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentService binaryContentService;
    private final UserStatusRepository userStatusRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;

    @Override
    public UserResponse create(UserCreateRequest request) {
        validateCreateRequest(request);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 이름입니다.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        UUID profileId = saveProfileImage(request.getProfileImage());

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );

        if (profileId != null) {
            user.updateProfileId(profileId);
        }

        User savedUser = userRepository.save(user);

        UserStatus userStatus = new UserStatus(savedUser.getId());
        userStatusRepository.save(userStatus);

        return toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse read(UUID id) {
        User user = findUserById(id);

        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> readAll() {
        List<User> users = userRepository.findAll();
        List<UserResponse> responses = new ArrayList<>();

        for (User user : users) {
            responses.add(toResponse(user));
        }

        return responses;
    }

    @Override
    public UserResponse update(UserUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("사용자 수정 요청은 비어 있을 수 없습니다.");
        }

        if (request.getId() == null) {
            throw new IllegalArgumentException("수정할 사용자 id는 null일 수 없습니다.");
        }

        User user = findUserById(request.getId());

        String updateUsername = isBlank(request.getUsername())
                ? user.getUsername()
                : request.getUsername();

        String updateEmail = isBlank(request.getEmail())
                ? user.getEmail()
                : request.getEmail();

        String updatePassword = isBlank(request.getPassword())
                ? user.getPassword()
                : request.getPassword();

        if (!user.getUsername().equals(updateUsername)
                && userRepository.existsByUsername(updateUsername)) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 이름입니다.");
        }

        if (!user.getEmail().equals(updateEmail)
                && userRepository.existsByEmail(updateEmail)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        user.update(
                updateUsername,
                updateEmail,
                updatePassword
        );

        if (request.getProfileImage() != null) {
            UUID oldProfileId = user.getProfileId();

            UUID newProfileId = saveProfileImage(request.getProfileImage());
            user.updateProfileId(newProfileId);

            if (oldProfileId != null) {
                binaryContentService.delete(oldProfileId);
            }
        } else if (request.getProfileId() != null) {
            if (!binaryContentRepository.existsById(request.getProfileId())) {
                throw new IllegalArgumentException("연결할 프로필 이미지를 찾을 수 없습니다. profileId=" + request.getProfileId());
            }

            user.updateProfileId(request.getProfileId());
        }

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    @Override
    public void delete(UUID id) {
        User user = findUserById(id);

        deleteMessageAttachmentsByAuthorId(id);

        messageRepository.deleteByAuthorId(id);

        readStatusRepository.deleteByUserId(id);

        if (user.getProfileId() != null) {
            binaryContentService.delete(user.getProfileId());
        }

        userStatusRepository.deleteByUserId(id);

        userRepository.deleteById(id);
    }

    private User findUserById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("사용자 id는 null일 수 없습니다.");
        }

        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "사용자를 찾을 수 없습니다. id=" + id
                ));
    }

    private void validateCreateRequest(UserCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("사용자 생성 요청은 비어 있을 수 없습니다.");
        }

        if (isBlank(request.getUsername())) {
            throw new IllegalArgumentException("사용자 이름은 비어 있을 수 없습니다.");
        }

        if (isBlank(request.getEmail())) {
            throw new IllegalArgumentException("이메일은 비어 있을 수 없습니다.");
        }

        if (isBlank(request.getPassword())) {
            throw new IllegalArgumentException("비밀번호는 비어 있을 수 없습니다.");
        }
    }

    private void deleteMessageAttachmentsByAuthorId(UUID authorId) {
        List<Message> messages = messageRepository.findAll();

        Set<UUID> attachmentIds = new HashSet<>();

        for (Message message : messages) {
            if (message.getAuthorId() == null || !message.getAuthorId().equals(authorId)) {
                continue;
            }

            if (message.getAttachmentIds() == null || message.getAttachmentIds().isEmpty()) {
                continue;
            }

            attachmentIds.addAll(message.getAttachmentIds());
        }

        for (UUID attachmentId : attachmentIds) {
            if (attachmentId != null) {
                binaryContentService.delete(attachmentId);
            }
        }
    }

    private UUID saveProfileImage(BinaryContentCreateRequest profileImage) {
        if (profileImage == null) {
            return null;
        }

        BinaryContentResponse savedProfile =
                binaryContentService.create(profileImage);

        return savedProfile.getId();
    }

    private UserResponse toResponse(User user) {
        BinaryContentResponse profileResponse = toProfileResponse(user.getProfileId());

        return new UserResponse(
                user.getId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileId(),
                profileResponse,
                getOnlineStatus(user.getId())
        );
    }

    private BinaryContentResponse toProfileResponse(UUID profileId) {
        if (profileId == null) {
            return null;
        }

        return binaryContentRepository.findById(profileId)
                .map(this::toBinaryContentResponse)
                .orElse(null);
    }

    private BinaryContentResponse toBinaryContentResponse(BinaryContent binaryContent) {
        return new BinaryContentResponse(
                binaryContent.getId(),
                binaryContent.getCreatedAt(),
                binaryContent.getUpdatedAt(),
                binaryContent.getFileName(),
                binaryContent.getContentType(),
                binaryContent.getSize()
        );
    }

    private boolean getOnlineStatus(UUID userId) {
        try {
            UserStatus userStatus = userStatusRepository.findByUserId(userId);

            if (userStatus == null) {
                log.warn("UserStatus가 존재하지 않아 offline 상태로 응답합니다. userId={}", userId);
                return false;
            }

            return userStatus.isOnline();
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "UserStatus 조회 중 오류가 발생했습니다. userId=" + userId,
                    e
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}