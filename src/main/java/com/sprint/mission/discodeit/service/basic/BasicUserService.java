package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
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

import java.util.*;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public UserResponse create(UserCreateRequest request) {
        if(userRepository.findByUserName(request.userName())!=null) {
            throw new IllegalArgumentException("이미 사용중인 계정입니다: " + request.userName());
        }
        if(userRepository.findByEmail(request.email())!=null) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다: " + request.email());
        }

        UUID savedProfileId = profileCheck(request.profile());

        User user = new User(request.userName(), request.email(), request.password(), savedProfileId);
        userRepository.save(user);

        UserStatus userStatus = new UserStatus(user.getId());
        userStatusRepository.save(userStatus);

        return returnResponse(user, userStatus);
    }

    @Override
    public UserResponse find(UUID id) {
        User user = userRepository.findById(id);
        UserStatus userStatus = findByUserId(id);

        if (user==null || userStatus==null) {
            throw new IllegalArgumentException("존재하지 않는 계정입니다.");
        }

        return returnResponse(user, userStatus);
    }

    @Override
    public User findById(UUID id) {
        User user = userRepository.findById(id);
        if (user==null) {
            throw new IllegalArgumentException("존재하지 않는 계정입니다.");
        }
        return user;
    }

    @Override
    public List<UserResponse> findAll() {
        List<User> users = userRepository.findAll();
        List<UserStatus> userStatuses = userStatusRepository.findAll();

        List<UserResponse> userResponses = new ArrayList<>();
        for (User user : users) {
            for (UserStatus userStatus : userStatuses) {
                if(userStatus.getUserId().equals(user.getId())) {
                    UserResponse dto = returnResponse(user, userStatus);
                    userResponses.add(dto);
                    break;
                }
            }
        }

        return userResponses;
    }

    @Override
    public UserResponse update(UserUpdateRequest request) {
        User user = userRepository.findById(request.id());
        UserStatus userStatus = findByUserId(request.id());

        if (user==null || userStatus==null) {
            throw new IllegalArgumentException("존재하지 않는 계정입니다.");
        }

        UUID finalProfileId = user.getProfileId();
        if (request.profile() != null) {
            if (user.getProfileId() != null) {
                binaryContentRepository.delete(user.getProfileId());
            }

            finalProfileId = profileCheck(request.profile());
        }

        user.update(request.userName(), request.email(), request.password(), finalProfileId);
        userRepository.save(user);

        return returnResponse(user, userStatus);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.findById(id);
        if(user==null) {
            throw new IllegalArgumentException("존재하지 않는 계정입니다.");
        }

        if (user.getProfileId() != null) {
            binaryContentRepository.delete(user.getProfileId());
        }

        UserStatus userStatus = findByUserId(id);
        if(userStatus!=null) {
            userStatusRepository.delete(userStatus.getId());
        }
        userRepository.delete(id);
    }

    private UserStatus findByUserId(UUID id) {
        List<UserStatus> userStatuses = userStatusRepository.findAll();
        for (UserStatus status : userStatuses) {
            if (status.getUserId().equals(id)) {
                return status;
            }
        }
        return null;
    }

    private UUID profileCheck(BinaryContentCreateRequest profile) {
        UUID savedProfileId = null;
        if(profile!=null) {
            BinaryContent binaryContent = new BinaryContent(profile.fileName(), profile.contentType(), profile.size(), profile.bytes());
            binaryContentRepository.save(binaryContent);
            savedProfileId = binaryContent.getId();
        }
        return savedProfileId;
    }

    private UserResponse returnResponse(User user, UserStatus userStatus) {
        return new UserResponse(user.getId(), user.getUserName(), user.getEmail(), userStatus.isOnline());
    }
}
