package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserFindResponse;
import com.sprint.mission.discodeit.dto.response.UserUpdateResponse;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    //interface
    @Override
    public User createUser(UserCreateRequest request) {
        //입력값 검증 처리하겠습니다
        validateString(request.name());
        validateString(request.email());
        validateString(request.password());

        //중복된 이름, 이메일로 생성 요청을 한 경우 검증
        validateNameExists(request.name());
        validateEmailExists(request.email());

        //프로필 사진 경로 존재 시
        UUID binaryContentId = null;
        if (request.profileImagePath() != null && !request.profileImagePath().isBlank()) {
            //binaryContent 생성
            BinaryContent binaryContent = new BinaryContent(request.profileImagePath());
            binaryContentRepository.createBinaryContent(binaryContent);
            binaryContentId = binaryContent.getId();
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
        //입력값 검증 처리하겠습니다
        validateUUID(userId);

        //유저 검색
        User userTemp = userRepository.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));

        //유저 상태 검색
        UserStatus userStatus = userStatusRepository.findUserStatusByUserId(userId)
                .orElseThrow(() -> new RuntimeException("에러: 해당 유저의 온라인 상태를 불러올 수 없습니다."));

//        log.info(userTemp.toString());
        return UserFindResponse.from(userTemp, userStatus.isUserOnline());
    }

    @Override
    public List<UserFindResponse> findAllUsers() {
        //유저들 검색
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user ->
                        UserFindResponse.from(
                                user, userStatusRepository.findUserStatusByUserId(user.getId()).get().isUserOnline()
                        ))
                .toList();
    }

    @Override
    public UserUpdateResponse updateUser(UUID userId, UserUpdateRequest request) {
        //입력값 검증 처리하겠습니다
        validateUUID(userId);
        validateString(request.newName());
        validateString(request.newEmail());
        validateString(request.newPassword());

        //유저 검색
        User userTemp = userRepository.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));

        //프로필 사진 경로 존재 시
        UUID binaryContentId = null;
        if (request.profileImagePath() != null && !request.profileImagePath().isBlank()) {
            //BinaryContent 검색
            BinaryContent binaryContent = binaryContentRepository.findBinaryContentByContentPath(request.profileImagePath())
                    .orElse(null);

            //BinaryContent 없으면
            if (binaryContent == null) {
                //binaryContent 생성
                binaryContent = new BinaryContent(request.profileImagePath());
                binaryContentRepository.createBinaryContent(binaryContent);
                //기존 유저 프로필 이미지 삭제
                deleteProfileImage(userId);
            }

            binaryContentId = binaryContent.getId();
        }

        //유저 업데이트
        userTemp.updateUser(request.newName(), request.newEmail(), request.newPassword(), binaryContentId);
        userRepository.save();

        return UserUpdateResponse.from(userTemp);
    }

    @Override
    public void deleteUser(UUID userId) {
        //입력값 검증 처리하겠습니다
        validateUUID(userId);

        //유저 검색
        User userTemp = userRepository.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));

        //유저 상태 검색 및 삭제
        UserStatus userStatus = userStatusRepository.findUserStatusByUserId(userId)
                .orElseThrow(() -> new RuntimeException("에러: 해당 유저의 온라인 상태를 불러올 수 없습니다."));
        userStatusRepository.deleteUserStatus(userStatus.getId());

        //기존 유저 프로필 이미지 삭제
        deleteProfileImage(userId);

        //유저 삭제
        userRepository.deleteUser(userId);

        log.info("유저: {}가 삭제됨.", userTemp.getName());
    }


    //유저의 현재 프로필 이미지가 존재한다면 삭제하기
    private void deleteProfileImage(UUID userId) {
        UUID binaryContentId = userRepository.findUserById(userId).get().getProfileId();

        if (binaryContentId != null) {
            binaryContentRepository.deleteBinaryContent(binaryContentId);
        }
    }
    // 들어온 String 필드가 null 혹은 공백인지 검증하는 메서드
    private void validateString(String str) {
        if (str == null || str.isBlank()) {
            throw new IllegalArgumentException("에러: 입력값이 Null 또는 공백입니다.");
        }
    }
    // 들어온 UUID 필드가 null인지 검증하는 메서드
    private void validateUUID(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("에러: 입력값이 Null입니다.");
        }
    }
    // 들어온 이름 필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateNameExists(String name) {
        if (userRepository.existsUserByName(name)) {
            throw new RuntimeException("이름: " + name + "은 이미 사용중입니다.");
        }
    }
    // 들어온 이메일 필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateEmailExists(String email) {
        if (userRepository.existsUserByEmail(email)) {
            throw new RuntimeException("이메일: " + email + "은 이미 사용중입니다.");
        }
    }
}
