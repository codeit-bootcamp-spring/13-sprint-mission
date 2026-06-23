package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
//Service 계층 구현체. 실제 데이터 저장은 Repository에 위임함
public class BasicUserService implements UserService {
    private final UserRepository userRepository; //사용자 저장소
    private final UserStatusRepository userStatusRepository; //사용자 상태(온라인 여부 등) 저장소
    private final BinaryContentRepository binaryContentRepository; //프로필 이미지 등의 파일 저장소

    @Override //사용자 생성
    public User create(UserCreateRequest userCreateRequest, Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
        //요청 객체에서 사용자명과 이메일 추출
        String username = userCreateRequest.getUsername();
        String email = userCreateRequest.getEmail();

        //1.username 중복 검사
        if(userRepository.existsByUsername(username)){
            throw new IllegalStateException("User with username " + username + " already exists");
        }
        //2.email 중복 검사
        if (userRepository.existsByEmail(email)){
            throw new IllegalStateException("User with email " + email + " already exists");
        }

        //3.프로필 이미지가 있을 경우 BinaryContent 생성 후 저장
        UUID nullableProfileId = optionalProfileCreateRequest
                .map(profileRequest -> {
                    String fileName = profileRequest.getFileName(); //업로드 파일
                    String contentType = profileRequest.getContentType(); //MIME 타입
                    byte[] bytes = profileRequest.getBytes(); //실제 파일 데이터(Byte 배열)
                    BinaryContent binaryContent = new BinaryContent(fileName, contentType, bytes); //BinaryContent 객체 생성
                    return binaryContentRepository.save(binaryContent).getId(); //저장 후 생성된 파일 ID 반환
                })
                .orElse(null); //프로필 이미지가 없으면 null 저장
        String password = userCreateRequest.getPassword(); //비밀번호 조회

        //4.user 객체 생성
        User user = new User(username,email,password,nullableProfileId);
        //5.DB(Repository)에 사용자 저장
        User createdUser = userRepository.save(user);

        //6.사용자 생성 시 기본 상태(userStatus) 생성
        Instant now = Instant.now();
        UserStatus userStatus = new UserStatus(createdUser.getId(),now);
        userStatusRepository.save(userStatus);
        return createdUser; //생성된 사용자 반환
    }

    @Override //사용자 단건 조회
    public UserDto find(UUID userId) {
        return userRepository.findById(userId)
                .map(this::toDto)
                .orElseThrow(()->new NoSuchElementException("User with id " + userId + " not found"));
    }

    @Override //전체 사용자 조회. Repository가 가진 모든 User 반환
    public List<UserDto> findAll() {
        return userRepository.findAll()
                .stream().map(this::toDto).toList();
    }

    @Override //사용자 정보 수정
    public User update(UUID userId, UserUpdateRequest userUpdateRequest, Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
        //1.기존 사용자 조회(수정 대상 사용자 조회)
        User user = userRepository.findById(userId) //기존 사용자 조회
                .orElseThrow(()-> new NoSuchElementException("User with id " + userId + " not found"));

        //수정할 사용자명, 이메일 조회
        String newUsername = userUpdateRequest.getNewUsername();
        String newEmail = userUpdateRequest.getNewEmail();
        //2.username 중복 검사
        userRepository.findByUsername(newUsername)
                .ifPresent(found -> {
                    //자기 자신이 아닌 다른 사용자가 사용 중이면 예외
                    if(!found.getId().equals(userId)){
                        throw new IllegalStateException("User with username " + newUsername + " already exists");
                    }
                });
        //3.email 중복 검사
        userRepository.findByEmail(newEmail)
                .ifPresent(found -> {
                    //자기 자신의 이메일이 아니 경우 중복 처리
                    if (!found.getId().equals(userId)) {
                        throw new IllegalStateException("User with email " + newEmail + " already exists");
                    }
                });


        //4.새 프로필 이미지 처리(있을 경우)
        UUID nullableProfileId = optionalProfileCreateRequest
                .map(profileRequest -> {
                    //기존 프로필이 있으면 삭제
                    Optional.ofNullable(user.getProfileId())
                            .ifPresent(binaryContentRepository::deleteById);
                    //새 파일 정보 조회
                    String fileName = profileRequest.getFileName();
                    String contentType = profileRequest.getContentType();
                    byte[] bytes = profileRequest.getBytes();
                    //새 프로필 생성
                    BinaryContent binaryContent = new BinaryContent(fileName, contentType, bytes);
                    //저장 후 새 프로필 ID 반환
                    return binaryContentRepository.save(binaryContent).getId();
                })
                .orElse(null); //프로필 변경이 없으면 null

        String newPassword = userUpdateRequest.getNewPassword(); //새 비밀번호 조회
        user.update(newUsername, newEmail, newPassword, nullableProfileId); //User 엔티티 내부 update 메서드 실행
        return userRepository.save(user); //변경된 객체 재저장(수정된 user 저장 후 반환)
    }

    @Override //사용자 삭제
    public void delete(UUID userId) {
        User user = userRepository.findById(userId) //삭제 대상 사용자 조회
                .orElseThrow(()-> new NoSuchElementException("User with id " + userId + " not found"));
        Optional.ofNullable(user.getProfileId()) //프로필 이미지가 존재하면 함께 삭제
                .ifPresent(binaryContentRepository::deleteById);
        userStatusRepository.deleteByUserId(userId); //사용자 상태 정보 삭제
        userRepository.deleteById(userId); //사용자 삭제
    }

    //user 엔티티를 userDto로 변환하는 메서드
    private UserDto toDto(User user) {
        Boolean online = userStatusRepository.findByUserId(user.getId()) //사용자 상태 조회 후 온라인 여부 추출
                .map(UserStatus::isOnline)
                .orElse(null);
         //DTO 생성 및 반환
        return new UserDto(
                user.getId(),
                user.getUpdatedAt(),
                user.getCreatedAt(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileId(),
                online
        );
    }
}
