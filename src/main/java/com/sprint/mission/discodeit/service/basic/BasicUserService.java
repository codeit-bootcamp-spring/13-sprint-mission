package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserFindResponse;
import com.sprint.mission.discodeit.dto.response.UserUpdateResponse;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.DuplicateResourceException;
import com.sprint.mission.discodeit.exception.FileException;
import com.sprint.mission.discodeit.exception.ObjectNotFoundException;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicUserService implements UserService {

    //필드
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;

    //interface
    @Override
    public User createUser(UserCreateRequest request, MultipartFile file) {
        //중복된 이름, 이메일로 생성 요청을 한 경우 검증
        validateNameExists(request.name());
        validateEmailExists(request.email());

        UUID binaryContentId = null;
        //프로필 사진 파일 존재 시
        if (file != null && !file.isEmpty()) {
            try {
                //binaryContent 생성
                BinaryContent binaryContent = new BinaryContent(
                        file.getOriginalFilename(),
                        (long) file.getBytes().length,
                        file.getContentType(),
                        file.getBytes()
                );
                binaryContentRepository.createBinaryContent(binaryContent);
                binaryContentId = binaryContent.getId();

            } catch (IOException e) {
                throw new FileException(e.getMessage());
            }
        }

        //유저 생성
        User user = new User(request.name(), request.email(), request.password(), binaryContentId);
        userRepository.createUser(user);
        log.info("유저: {}가 생성됨.", user.getName());

        //UserStatus 생성
        UserStatus userStatus = new UserStatus(user.getId());
        userStatusRepository.createUserStatus(userStatus);

        return user;
    }

    @Override
    public UserFindResponse findUser(UUID userId) {
        //유저 검색
        User userTemp = userRepository.findUserById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));

        //유저 상태 검색
        UserStatus userStatus = userStatusRepository.findUserStatusByUserId(userId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 유저의 온라인 상태를 불러올 수 없습니다."));

        return UserFindResponse.from(userTemp, userStatus.isUserOnline());
    }

    @Override
    public List<UserFindResponse> findAllUsers() {
        //유저들 검색
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user ->
                        UserFindResponse.from(
                                user, userStatusRepository.findUserStatusByUserId(user.getId())
                                        .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 유저의 온라인 상태를 불러올 수 없습니다.")).isUserOnline()
                        ))
                .toList();
    }

    @Override
    public UserUpdateResponse updateUser(UserUpdateRequest request, MultipartFile file) {
        //유저 검색
        User userTemp = userRepository.findUserById(request.userId())
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));

        //중복된 이름, 이메일로 수정 요청을 한 경우 검증
        if (!userTemp.getName().equals(request.newName())) {
            validateNameExists(request.newName());
        }
        if (!userTemp.getEmail().equals(request.newEmail())) {
            validateEmailExists(request.newEmail());
        }

        UUID binaryContentId = userTemp.getProfileId();
        //프로필 사진 파일 존재 시
        if (file != null && !file.isEmpty()) {
            //기존 프로필 이미지 삭제
            if (binaryContentId != null) {
                binaryContentRepository.deleteBinaryContent(binaryContentId);
            }

            try {
                //binaryContent 생성
                BinaryContent binaryContent = new BinaryContent(
                        file.getOriginalFilename(),
                        (long) file.getBytes().length,
                        file.getContentType(),
                        file.getBytes()
                );
                binaryContentRepository.createBinaryContent(binaryContent);
                binaryContentId = binaryContent.getId();

            } catch (IOException e) {
                throw new FileException(e.getMessage());
            }
        }

        log.info("유저: {}가 수정됨.", userTemp.getName());
        log.info("name: {}, email: {}, password: {}\n-> name: {}, email: {}, password: {}", userTemp.getName(), userTemp.getEmail(), userTemp.getPassword(), request.newName(), request.newEmail(), request.newPassword());

        //유저 업데이트
        userTemp.updateUser(request.newName(), request.newEmail(), request.newPassword(), binaryContentId);
        userRepository.save();

        return UserUpdateResponse.from(userTemp);
    }

    @Override
    public void deleteUser(UUID userId) {
        //유저 검색
        User userTemp = userRepository.findUserById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));

        //유저 상태 검색 및 삭제
        deleteUserStatus(userId);

        //유저가 가입한 채널에 대한 ReadStatus 검색 및 삭제
        deleteReadStatus(userId);

        //유저가 작성한 메세지 검색 및 삭제
        deleteUserMessages(userId);

        //기존 유저 프로필 이미지 삭제
        deleteProfileImage(userTemp);

        //유저 삭제
        userRepository.deleteUser(userId);

        log.info("유저: {}가 삭제됨.", userTemp.getName());
    }

    //유저 상태 검색 및 삭제
    private void deleteUserStatus(UUID userId) {
        UserStatus userStatus = userStatusRepository.findUserStatusByUserId(userId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 유저의 온라인 상태를 불러올 수 없습니다."));
        userStatusRepository.deleteUserStatus(userStatus.getId());
    }

    //유저가 가입한 채널에 대한 ReadStatus 검색 및 삭제
    private void deleteReadStatus(UUID userId) {
        List<ReadStatus> readStatusList = readStatusRepository.findAllReadStatusByUserId(userId);
        for (ReadStatus readStatus : readStatusList) {
            readStatusRepository.deleteReadStatusById(readStatus.getId());
        }
    }

    //유저가 작성한 메세지 검색 및 삭제
    private void deleteUserMessages(UUID userId) {
        List<Message> messageList = messageRepository.findAllMessagesByUserId(userId);
        for (Message message : messageList) {
            for (UUID attachmentId : message.getAttachmentIds()) {
                binaryContentRepository.deleteBinaryContent(attachmentId);
            }
            messageRepository.deleteMessageById(message.getId());
        }
    }

    //유저의 현재 프로필 이미지가 존재한다면 삭제하기
    private void deleteProfileImage(User user) {
        UUID binaryContentId = user.getProfileId();

        if (binaryContentId != null) {
            binaryContentRepository.deleteBinaryContent(binaryContentId);
        }
    }

    // 들어온 이름 필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateNameExists(String name) {
        if (userRepository.existsUserByName(name)) {
            throw new DuplicateResourceException("이름: " + name + "은 이미 사용중입니다.");
        }
    }

    // 들어온 이메일 필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateEmailExists(String email) {
        if (userRepository.existsUserByEmail(email)) {
            throw new DuplicateResourceException("이메일: " + email + "은 이미 사용중입니다.");
        }
    }
}
