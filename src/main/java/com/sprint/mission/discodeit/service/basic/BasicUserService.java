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

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;


    @Override
    public UserResponse create(UserCreateRequest createRequest) {

        validateUniqueUser(
                createRequest.username(),
                createRequest.email());

        UUID profileId = null;
        if (createRequest.profileBytes() != null) {
            BinaryContent profile = new BinaryContent(
                    createRequest.profileName(),
                    createRequest.profileContentType(),
                    createRequest.profileBytes()
            );
            binaryContentRepository.save(profile);
            profileId = profile.getId();
        }

       User user = new User(
               createRequest.username(),
               createRequest.email(),
               createRequest.password(),
               profileId
       );

       userRepository.save(user);

        UserStatus userStatus = new UserStatus(user.getId(), Instant.now());

        userStatusRepository.save(userStatus);

       return toResponse(user);
    }

    @Override
    public UserResponse findById(UUID id) {
        User user = userRepository.findById(id);
        if (user == null) {
            return null;
        }
        return toResponse(user);
    }

    @Override
    public Collection<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse update(UserUpdateRequest updateRequest) {
        User user= userRepository.findById(updateRequest.id());

        validateUniqueUserForUpdate(
                updateRequest.id(),
                updateRequest.username(),
                updateRequest.email());


        UUID profileId = user.getProfileId();

        if (updateRequest.profileBytes() != null) {
            BinaryContent profile = new BinaryContent(
                    updateRequest.profileName(),
                    updateRequest.profileContentType(),
                    updateRequest.profileBytes()
            );
            binaryContentRepository.save(profile);
            profileId = profile.getId();
        }

        user.renew(
                updateRequest.username(),
                updateRequest.email(),
                updateRequest.password(),
                profileId);

        userRepository.save(user);
        return toResponse(user);
        }

    @Override
    public void delete(UUID id) {
        User user = userRepository.findById(id);

        UUID profileId = user.getProfileId();
        if (profileId != null) {
            binaryContentRepository.delete(profileId);
        }

        UserStatus userStatus = userStatusRepository.findByUserId(id);
        if (userStatus != null) {
            userStatusRepository.delete(userStatus.getId());
        }

        userRepository.delete(id);
    }

    private UserResponse toResponse(User user) {
        UserStatus userStatus = userStatusRepository.findByUserId(user.getId());

        boolean isOnline = userStatus != null && userStatus.isOnline();

        return  new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfileId(),
                isOnline);
    }

    private void validateUniqueUser (String username, String email) {

        if (userRepository.findByName(username) != null) {
            throw new IllegalArgumentException("이미 사용 중인 이름입니다.");
        }

        if (userRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    private void validateUniqueUserForUpdate(UUID id, String username, String email) {
        User userByName = userRepository.findByName(username);
        if (userByName != null && !userByName.getId().equals(id)) {
            throw new IllegalArgumentException("이미 사용 중인 username입니다.");
        }

        User userByEmail = userRepository.findByEmail(email);
        if (userByEmail != null && !userByEmail.getId().equals(id)) {
            throw new IllegalArgumentException("이미 사용 중인 email입니다.");
        }
    }

}
