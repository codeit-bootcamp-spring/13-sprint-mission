package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.time.Instant;
import java.util.*;

public class JCFUserService implements UserService {

    private final Map<UUID, User> data;

    public JCFUserService() {
        this.data = new HashMap<>();
    }

    @Override
    public UserResponse create(UserCreateRequest request) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .profileId(request.profileId())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        data.put(user.getId(), user);
        return UserResponse.from(user, false);
    }


    @Override
    public UserResponse findById(UUID id) {
        User user = data.get(id);
        if (user == null) {
            return null;
        }
        return UserResponse.from(user, false);
    }

    @Override
    public List<UserResponse> findAll() {
        return data.values().stream()
                .map(user -> UserResponse.from(user, false))
                .toList();
    }

    @Override
    public UserResponse update(UserUpdateRequest request) {
        User user = data.get(request.id());
        if (user == null) {
            return null;
        }

        user.update(
                request.username() != null ? request.username() : user.getUsername(),
                request.email() != null ? request.email() : user.getEmail(),
                request.password() != null ? request.password() : user.getPassword(),
                request.profileId() != null ? request.profileId() : user.getProfileId()
        );

        return UserResponse.from(user, false);
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}
