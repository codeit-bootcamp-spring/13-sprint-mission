package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FileUserService implements UserService {

    private final UserRepository repository;

    public FileUserService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserResponse create(UserCreateRequest request) {
        return null;
    }

    @Override
    public UserResponse findById(UUID id) {
        Optional<User> user = repository.findById(id);
        if (user.isEmpty()) {
            return null;
        }

        return UserResponse.from(user.orElse(null), false);
    }

    @Override
    public List<UserResponse> findAll() {
        return repository.findAll().stream()
                .map(user -> UserResponse.from(user, false))
                .toList();
    }

    @Override
    public UserResponse update(UserUpdateRequest request) {
        return null;
    }



    @Override
    public void delete(UUID id) {
        repository.delete(id);
    }
}
