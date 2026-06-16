package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor // 의존성 주입
@Service // Basic*Service 구현체를 Service 인터페이스의 Bean으로 등록
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final BinaryContentRepository contentRepository;
    private final UserStatusRepository statusRepository;

    @Override
    public UserResponse create(UserCreateRequest request) {
        // username과 email 다른 유저와 다른지 중복 검사
        boolean flag1 = userRepository.findAll().stream()
                .anyMatch(user -> user.getUsername().equals(request.username()));
        if (flag1){
            throw new IllegalArgumentException(request.username()+" 은(는) 중복된 이름입니다.");
        }
        boolean flag2 = userRepository.findAll().stream()
                .anyMatch(user -> user.getEmail().equals(request.email()));
        if (flag2){
            throw new IllegalArgumentException(request.email()+" 은(는) 중복된 이메일입니다.");
        }
        // 프로필 이미지 있으면 등록
        UUID profileId = null;
        if (request.profileImage() != null){
            BinaryContent profileImage=new BinaryContent(
                    request.profileImage().fileName(),
                    request.profileImage().contentType(),
                    request.profileImage().fileSize()
            );
            contentRepository.save(profileImage);
            profileId=profileImage.getId();
        }
        // 프로필 이미지 없으면 이는 비워두고 등록
        User user=new User(request.username(), request.email(),
                request.password(), request.profileImage() == null ? null : profileId);
        userRepository.save(user);
        // UserStatus를 같이 생성
        UserStatus userStatus=new UserStatus(user.getId());
        statusRepository.save(userStatus);
        return UserResponse.from(user, userStatus);
    }

    @Override
    public UserResponse find(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(userId + " (을)를 찾을 수 없습니다."));
        UserStatus userStatus=statusRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException(userId + " (을)를 찾을 수 없습니다."));
        return UserResponse.from(user, userStatus);
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserStatus userStatus =statusRepository.findByUserId(user.getId())
                            .orElseThrow(() -> new NoSuchElementException(user.getId() + " (을)를 찾을 수 없습니다."));
                    return UserResponse.from(user, userStatus);
                }).toList();
    }

    @Override
    public UserResponse update(UserUpdateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NoSuchElementException(request.userId() + " (을)를 찾을 수 없습니다."));
        UUID oldProfileId = user.getProfileId();
        UUID newProfileId = oldProfileId;
        // 프로필 이미지 선택적으로 대체
        if (request.newProfileImage() != null){
            BinaryContent newProfileImage =new BinaryContent(
                    request.newProfileImage().fileName(),
                    request.newProfileImage().contentType(),
                    request.newProfileImage().fileSize()
            );
            contentRepository.save(newProfileImage);
            newProfileId = newProfileImage.getId();
        }
        // 기존 프로필 삭제
        if (oldProfileId != null && !oldProfileId.equals(newProfileId)){
            contentRepository.deleteById(oldProfileId);
        }
        user.update(request.newUsername(), request.newEmail(), request.newPassword(), newProfileId); // profileId 추가
        userRepository.save(user);
        UserStatus userStatus = statusRepository.findByUserId(user.getId())
                .orElseThrow(()-> new NoSuchElementException(user.getId()+" (을)를 찾을 수 없습니다."));
        return UserResponse.from(user, userStatus);
    }

    @Override
    public void delete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(userId + " (을)를 찾을 수 없습니다."));
        // 관련된 도메인도 같이 삭제
        if (user.getProfileId() != null){
            contentRepository.deleteById(user.getProfileId());
        }
        statusRepository.findByUserId(userId)
                .ifPresent(userStatus -> statusRepository.deleteById(userStatus.getId()));
        userRepository.deleteById(userId);
    }
}
