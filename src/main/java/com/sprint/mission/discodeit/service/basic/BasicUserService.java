package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.repository.file.*;
import com.sprint.mission.discodeit.service.UserService;
import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.web.multipart.*;

import java.io.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository repository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public UserResponse create(UserRequest.CreateUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("유저 생성 요청은 필수입니다.");
        }

        if(request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("유저의 이름은 공백이면 안됩니다.");
        }

        if (repository.findByUserName(request.username()) != null) {
            throw new IllegalArgumentException("이미 사용 중인 유저이름입니다.");
        }

        if(request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("이메일은 공백이면 안됩니다.");
        }

        if(repository.findByEmail(request.email()) != null) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if(request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("비밀번호는 공백이면 안됩니다.");
        }

        User user = new User(
                request.username(),
                request.email(),
                request.password()
        );

        BinaryContent profile = null;
        MultipartFile profileImage = request.profileImage();

        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                profile = new BinaryContent(
                        user.getId(),
                        null,
                        profileImage.getContentType(),
                        profileImage.getBytes(),
                        profileImage.getOriginalFilename()
                );

                binaryContentRepository.create(profile);

                user.updateProfileId(profile.getId());

            } catch (IOException e) {
                throw new RuntimeException("프로필 이미지 파일을 읽는 중 오류가 발생했습니다.", e);
            }
        }
        repository.create(user);

        if (repository instanceof FileUserRepository) {
            ((FileUserRepository) repository).saveToFile();
        }

        UserStatus userStatus = new UserStatus(user.getId());
        userStatusRepository.create(userStatus);

        return UserResponse.from(user, userStatus, profile);
    }


    @Override
    public UserResponse find(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID를 찾을 수가 없습니다.");
        }

        User user = repository.find(id);

        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저 ID입니다.");
        }

        UserStatus userStatus = userStatusRepository.findByUserId(id);
        BinaryContent profile = binaryContentRepository.findByUserId(id);

        return UserResponse.from(user, userStatus, profile);

    }

    @Override
    public List<UserDto> findAll() {
        return repository.findAll().stream()
                .map (user -> {
                UserStatus userStatus = userStatusRepository.findByUserId(user.getId());
                BinaryContent profile = binaryContentRepository.findByUserId(user.getId());

                return UserDto.from(user, userStatus, profile);
        }).toList();
    }

    @Override
    public UserResponse update(UUID id, UserRequest.UpdateUserRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }

        if (request == null) {
            throw new IllegalArgumentException("유저 수정 요청은 필수입니다.");
        }

        User user = repository.find(id);

        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저 ID입니다.");
        }

        if (repository.findByEmail(request.email()) != null) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        if (repository.findByUserName(request.username()) != null) {
            throw new IllegalArgumentException("이미 사용중인 유저 이름입니다.");
        }

        user.updateUserName(request.username());
        user.updateEmail(request.email());
        user.updatePassWord(request.password());

        repository.update(id, user);

        UserStatus userStatus = userStatusRepository.findByUserId(id);
        BinaryContent profile = binaryContentRepository.findByUserId(id);

        return UserResponse.from(user, userStatus, profile);
    }

    @Override
    public void delete(UUID id) {
        System.out.println("delete user id = " + id);

        if (id == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }

        if (!repository.exists(id)) {
            throw new IllegalArgumentException("존재하지 않는 유저 ID입니다.");
        }

        UserStatus userStatus = userStatusRepository.findByUserId(id);
        BinaryContent profile = binaryContentRepository.findByUserId(id);

        System.out.println("userStatus = " + userStatus);
        System.out.println("profile = " + profile);

        if (profile != null) {
            System.out.println("delete profile id = " + profile.getId());
            binaryContentRepository.delete(profile.getId());
        }

        if (userStatus != null) {
            System.out.println("delete userStatus id = " + userStatus.getId());
            userStatusRepository.delete(userStatus.getId());
        }

        System.out.println("delete user");
        repository.delete(id);
    }
}