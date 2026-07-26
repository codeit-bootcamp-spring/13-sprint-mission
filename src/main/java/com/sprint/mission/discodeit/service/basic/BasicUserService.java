package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.file.FileStorageException;
import com.sprint.mission.discodeit.exception.user.UserEmailAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNameAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final UserMapper userMapper;
    private final BinaryContentStorage binaryContentStorage;

    //interface
    @Override
    @Transactional
    public UserDto createUser(UserCreateRequest request, MultipartFile file) {
        log.debug("유저 생성 시작");

        //중복된 이름, 이메일로 생성 요청을 한 경우 검증
        validateNameExists(request.username());
        validateEmailExists(request.email());

        BinaryContent binaryContent = null;
        //프로필 사진 파일 존재 시
        if (file != null && !file.isEmpty()) {
            log.debug("프로필 파일 업로드 처리 시작");
            //binaryContent 생성
            binaryContent = createBinaryContent(file);

            log.info("프로필 파일 업로드 완료");
        }

        //유저 생성
        User user = new User(request.username(), request.email(), request.password(), binaryContent);
        log.info("유저: {}가 생성됨.", user.getUsername());

        //UserStatus 생성
        UserStatus userStatus = new UserStatus(user);
//        userStatus = userStatusRepository.save(userStatus);

        user.assignStatus(userStatus);
        user = userRepository.save(user);

        log.info("유저 생성 완료");

        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUser(UUID userId) {
        //유저 검색
        User userTemp = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return userMapper.toDto(userTemp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers() {
        //유저들 검색
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto updateUser(UUID userId, UserUpdateRequest request, MultipartFile file) {
        log.debug("유저 수정 시작");

        //유저 검색
        User userTemp = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("유저 수정 실패: 해당 유저는 데이터파일에 존재하지 않습니다.");
                    return new UserNotFoundException(userId);
                });

        //중복된 이름, 이메일로 수정 요청을 한 경우 검증
        if (!userTemp.getUsername().equals(request.newUsername())) {
            validateNameExists(request.newUsername());
        }
        if (!userTemp.getEmail().equals(request.newEmail())) {
            validateEmailExists(request.newEmail());
        }

        BinaryContent binaryContent = userTemp.getProfile();
        //프로필 사진 파일 존재 시
        if (file != null && !file.isEmpty()) {
            log.debug("프로필 파일 업로드 처리 시작");
            //기존 프로필 이미지 삭제
            if (binaryContent != null) {
                binaryContentRepository.deleteById(binaryContent.getId());
            }
            //binaryContent 생성
            binaryContent = createBinaryContent(file);

            log.info("프로필 파일 업로드 완료");
        }

        log.info("유저: {}가 수정됨.", userTemp.getUsername());
        log.info("name: {}, email: {}\n-> name: {}, email: {}", userTemp.getUsername(), userTemp.getEmail(), request.newUsername(), request.newEmail());

        //유저 업데이트
        userTemp.updateUser(request.newUsername(), request.newEmail(), request.newPassword(), binaryContent);
        //dirty checking
        //userTemp = userRepository.save(userTemp);

        log.info("유저 수정 완료");

        return userMapper.toDto(userTemp);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        log.debug("유저 삭제 시작");

        //유저 검색
        User userTemp = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("유저 삭제 실패: 해당 유저는 데이터파일에 존재하지 않습니다.");
                    return new UserNotFoundException(userId);
                });

        //유저 상태 검색 및 삭제
        //UserStatus는 cascade로 함께 삭제되므로 별도 삭제하지 않음
        //deleteUserStatus(userId);

        //유저가 가입한 채널에 대한 ReadStatus 검색 및 삭제
        deleteReadStatus(userId);

        //유저가 작성한 메세지 검색 및 삭제
        deleteUserMessages(userId);

        //기존 유저 프로필 이미지 삭제
        deleteProfileImage(userTemp);

        //유저 삭제
        userRepository.deleteById(userId);

        log.info("유저: {}가 삭제됨.", userTemp.getUsername());

        log.info("유저 삭제 완료");
    }

    //binaryContent 생성
    private BinaryContent createBinaryContent(MultipartFile file) {
        BinaryContent binaryContent = null;
        try {
            //binaryContent 생성
            binaryContent = new BinaryContent(
                    file.getOriginalFilename(),
                    (long) file.getBytes().length,
                    file.getContentType()
            );
            binaryContent = binaryContentRepository.save(binaryContent);
            binaryContentStorage.put(binaryContent.getId(), file.getBytes());

        } catch (IOException e) {
            log.error("프로필 파일 업로드 실패");
            throw new FileStorageException(file.getOriginalFilename());
        }

        return binaryContent;
    }


    //유저가 가입한 채널에 대한 ReadStatus 검색 및 삭제
    private void deleteReadStatus(UUID userId) {
        List<ReadStatus> readStatusList = readStatusRepository.findAllByUserId(userId);
        for (ReadStatus readStatus : readStatusList) {
            readStatusRepository.deleteById(readStatus.getId());
        }
    }

    //유저가 작성한 메세지 검색 및 삭제
    private void deleteUserMessages(UUID userId) {
        List<Message> messageList = messageRepository.findAllByAuthorId(userId);
        for (Message message : messageList) {
            for (BinaryContent attachment : message.getAttachments()) {
                binaryContentRepository.deleteById(attachment.getId());
            }
            messageRepository.deleteById(message.getId());
        }
    }

    //유저의 현재 프로필 이미지가 존재한다면 삭제하기
    private void deleteProfileImage(User user) {
        UUID binaryContentId = user.getProfile().getId();

        if (binaryContentId != null) {
            binaryContentRepository.deleteById(binaryContentId);
        }
    }

    // 들어온 이름 필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateNameExists(String name) {
        if (userRepository.existsByUsername(name)) {
            log.warn("이름: {}은 이미 사용중입니다.", name);
            throw new UserNameAlreadyExistsException(name);
        }
    }

    // 들어온 이메일 필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateEmailExists(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("이메일: {}은 이미 사용중입니다.", email);
            throw new UserEmailAlreadyExistsException(email);
        }
    }
}
