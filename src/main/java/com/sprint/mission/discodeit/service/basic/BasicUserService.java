package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentResponse;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
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
        log.info("사용자 생성을 시작합니다.");

        validateCreateRequest(request);

        log.debug(
                "사용자 생성 요청을 검증합니다. username={}, email={}, hasProfileImage={}",
                request.getUsername(),
                request.getEmail(),
                request.getProfileImage() != null
        );

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn(
                    "사용자 생성에 실패했습니다. 이미 사용 중인 사용자 이름입니다. username={}",
                    request.getUsername()
            );

            throw new UserAlreadyExistsException(
                    ErrorCode.DUPLICATE_USERNAME,
                    "username",
                    request.getUsername()
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn(
                    "사용자 생성에 실패했습니다. 이미 사용 중인 이메일입니다. email={}",
                    request.getEmail()
            );

            throw new UserAlreadyExistsException(
                    ErrorCode.DUPLICATE_EMAIL,
                    "email",
                    request.getEmail()
            );
        }

        try {
            BinaryContent profile =
                    saveProfileImage(request.getProfileImage());

            UserData userData = new UserData(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword()
            );

            User user = new User(userData);

            if (profile != null) {
                user.updateProfile(profile);

                log.debug(
                        "사용자 프로필 이미지를 연결합니다. profileId={}",
                        profile.getId()
                );
            }

            User savedUser = userRepository.save(user);

            UserStatus userStatus = new UserStatus(savedUser.getId());
            userStatusRepository.save(userStatus);

            log.info(
                    "사용자 생성이 완료되었습니다. userId={}, username={}",
                    savedUser.getId(),
                    savedUser.getUsername()
            );

            return toResponse(savedUser);

        } catch (RuntimeException exception) {
            log.error(
                    "사용자 생성 중 오류가 발생했습니다. username={}, email={}",
                    request.getUsername(),
                    request.getEmail(),
                    exception
            );
            throw exception;
        }
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
        log.info(
                "사용자 수정을 시작합니다. userId={}",
                request == null ? null : request.getId()
        );

        if (request == null) {
            log.warn("사용자 수정 요청이 비어 있습니다.");
            throw new IllegalArgumentException(
                    "사용자 수정 요청은 비어 있을 수 없습니다."
            );
        }

        if (request.getId() == null) {
            log.warn("수정할 사용자 id가 null입니다.");
            throw new IllegalArgumentException(
                    "수정할 사용자 id는 null일 수 없습니다."
            );
        }

        User user = findUserById(request.getId());

        log.debug(
                "사용자 수정 요청을 처리합니다. userId={}, usernameChanged={}, emailChanged={}, passwordChanged={}, profileImageChanged={}, profileIdChanged={}",
                request.getId(),
                !isBlank(request.getUsername()),
                !isBlank(request.getEmail()),
                !isBlank(request.getPassword()),
                request.getProfileImage() != null,
                request.getProfileId() != null
        );

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

            log.warn(
                    "사용자 수정에 실패했습니다. 이미 사용 중인 사용자 이름입니다. userId={}, username={}",
                    request.getId(),
                    updateUsername
            );

            throw new UserAlreadyExistsException(
                    ErrorCode.DUPLICATE_USERNAME,
                    "username",
                    updateUsername
            );
        }

        if (!user.getEmail().equals(updateEmail)
                && userRepository.existsByEmail(updateEmail)) {

            log.warn(
                    "사용자 수정에 실패했습니다. 이미 사용 중인 이메일입니다. userId={}, email={}",
                    request.getId(),
                    updateEmail
            );

            throw new UserAlreadyExistsException(
                    ErrorCode.DUPLICATE_EMAIL,
                    "email",
                    updateEmail
            );
        }

        try {
            UserData userData = new UserData(
                    updateUsername,
                    updateEmail,
                    updatePassword
            );

            user.update(userData);

            if (request.getProfileImage() != null) {
                UUID oldProfileId = user.getProfileId();

                BinaryContent newProfile =
                        saveProfileImage(request.getProfileImage());

                user.updateProfile(newProfile);

                log.debug(
                        "사용자 프로필 이미지를 교체합니다. userId={}, oldProfileId={}, newProfileId={}",
                        user.getId(),
                        oldProfileId,
                        newProfile == null ? null : newProfile.getId()
                );

                if (oldProfileId != null) {
                    binaryContentService.delete(oldProfileId);
                }

            } else if (request.getProfileId() != null) {
                BinaryContent profile =
                        binaryContentRepository
                                .findById(request.getProfileId())
                                .orElseThrow(() -> {
                                    log.warn(
                                            "연결할 프로필 이미지를 찾을 수 없습니다. userId={}, profileId={}",
                                            user.getId(),
                                            request.getProfileId()
                                    );

                                    return new BinaryContentNotFoundException(
                                            request.getProfileId()
                                    );
                                });

                user.updateProfile(profile);

                log.debug(
                        "기존 프로필 이미지를 사용자에게 연결합니다. userId={}, profileId={}",
                        user.getId(),
                        profile.getId()
                );
            }

            User savedUser = userRepository.save(user);

            log.info(
                    "사용자 수정이 완료되었습니다. userId={}",
                    savedUser.getId()
            );

            return toResponse(savedUser);

        } catch (RuntimeException exception) {
            log.error(
                    "사용자 수정 중 오류가 발생했습니다. userId={}",
                    request.getId(),
                    exception
            );
            throw exception;
        }
    }

    @Override
    public void delete(UUID id) {
        log.info("사용자 삭제를 시작합니다. userId={}", id);

        User user = findUserById(id);
        UUID profileId = user.getProfileId();

        try {
            log.debug(
                    "사용자 관련 데이터를 삭제합니다. userId={}, profileId={}",
                    id,
                    profileId
            );

            deleteMessageAttachmentsByAuthorId(id);

            messageRepository.deleteByAuthor_Id(id);
            readStatusRepository.deleteByUser_Id(id);
            userStatusRepository.deleteByUserId(id);
            userRepository.deleteById(id);

            if (profileId != null) {
                binaryContentService.delete(profileId);
            }

            log.info(
                    "사용자 삭제가 완료되었습니다. userId={}",
                    id
            );

        } catch (RuntimeException exception) {
            log.error(
                    "사용자 삭제 중 오류가 발생했습니다. userId={}",
                    id,
                    exception
            );
            throw exception;
        }
    }

    private User findUserById(UUID id) {
        if (id == null) {
            log.warn(
                    "사용자 조회에 실패했습니다. userId가 null입니다."
            );

            throw new IllegalArgumentException(
                    "사용자 id는 null일 수 없습니다."
            );
        }

        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "사용자를 찾을 수 없습니다. userId={}",
                            id
                    );

                    return new UserNotFoundException(id);
                });
    }

    private void validateCreateRequest(UserCreateRequest request) {
        if (request == null) {
            log.warn("사용자 생성 요청이 비어 있습니다.");

            throw new IllegalArgumentException(
                    "사용자 생성 요청은 비어 있을 수 없습니다."
            );
        }

        if (isBlank(request.getUsername())) {
            log.warn(
                    "사용자 생성에 실패했습니다. 사용자 이름이 비어 있습니다."
            );

            throw new IllegalArgumentException(
                    "사용자 이름은 비어 있을 수 없습니다."
            );
        }

        if (isBlank(request.getEmail())) {
            log.warn(
                    "사용자 생성에 실패했습니다. 이메일이 비어 있습니다."
            );

            throw new IllegalArgumentException(
                    "이메일은 비어 있을 수 없습니다."
            );
        }

        if (isBlank(request.getPassword())) {
            log.warn(
                    "사용자 생성에 실패했습니다. 비밀번호가 비어 있습니다."
            );

            throw new IllegalArgumentException(
                    "비밀번호는 비어 있을 수 없습니다."
            );
        }
    }

    private void deleteMessageAttachmentsByAuthorId(UUID authorId) {
        log.debug(
                "사용자가 작성한 메시지 첨부파일 삭제를 시작합니다. authorId={}",
                authorId
        );

        List<Message> messages =
                messageRepository.findAllByAuthor_Id(authorId);

        log.debug(
                "사용자가 작성한 메시지를 조회했습니다. authorId={}, messageCount={}",
                authorId,
                messages.size()
        );

        Set<UUID> attachmentIds = new HashSet<>();

        for (Message message : messages) {
            List<UUID> messageAttachmentIds =
                    message.getAttachmentIds();

            if (messageAttachmentIds == null
                    || messageAttachmentIds.isEmpty()) {
                continue;
            }

            attachmentIds.addAll(messageAttachmentIds);
        }

        log.debug(
                "삭제할 메시지 첨부파일을 수집했습니다. authorId={}, attachmentCount={}",
                authorId,
                attachmentIds.size()
        );

        for (UUID attachmentId : attachmentIds) {
            if (attachmentId == null) {
                continue;
            }

            log.debug(
                    "메시지 첨부파일을 삭제합니다. authorId={}, attachmentId={}",
                    authorId,
                    attachmentId
            );

            binaryContentService.delete(attachmentId);
        }

        log.info(
                "사용자 메시지 첨부파일 삭제가 완료되었습니다. authorId={}, deletedCount={}",
                authorId,
                attachmentIds.size()
        );
    }

    private BinaryContent saveProfileImage(
            BinaryContentCreateRequest profileImage
    ) {
        if (profileImage == null) {
            log.debug("저장할 프로필 이미지가 없습니다.");
            return null;
        }

        log.debug(
                "프로필 이미지 저장을 시작합니다. fileName={}, contentType={}",
                profileImage.getFileName(),
                profileImage.getContentType()
        );

        BinaryContentResponse savedProfile =
                binaryContentService.create(profileImage);

        BinaryContent profile =
                binaryContentRepository
                        .findById(savedProfile.getId())
                        .orElseThrow(() -> {
                            log.error(
                                    "저장된 프로필 이미지 메타데이터를 찾을 수 없습니다. profileId={}",
                                    savedProfile.getId()
                            );

                            return new IllegalStateException(
                                    "저장된 프로필 이미지를 찾을 수 없습니다. profileId="
                                            + savedProfile.getId()
                            );
                        });

        log.debug(
                "프로필 이미지 저장이 완료되었습니다. profileId={}, fileName={}",
                profile.getId(),
                profile.getFileName()
        );

        return profile;
    }

    private UserResponse toResponse(User user) {
        BinaryContent profile = user.getProfile();

        BinaryContentResponse profileResponse =
                toBinaryContentResponse(profile);

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

    private BinaryContentResponse toBinaryContentResponse(
            BinaryContent binaryContent
    ) {
        if (binaryContent == null) {
            return null;
        }

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
            UserStatus userStatus =
                    userStatusRepository.findByUserId(userId);

            if (userStatus == null) {
                log.warn(
                        "UserStatus가 존재하지 않아 offline 상태로 응답합니다. userId={}",
                        userId
                );
                return false;
            }

            return userStatus.isOnline();

        } catch (RuntimeException exception) {
            log.error(
                    "UserStatus 조회 중 오류가 발생했습니다. userId={}",
                    userId,
                    exception
            );

            throw new IllegalStateException(
                    "UserStatus 조회 중 오류가 발생했습니다. userId="
                            + userId,
                    exception
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}