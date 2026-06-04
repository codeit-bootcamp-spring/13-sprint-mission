package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;

import java.util.*;

public class JCFUserService implements UserService {

    private final UserRepository repository;

    public JCFUserService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void create(User user) {
        if (user == null) {
            throw new IllegalArgumentException("유저 정보가 없습니다.");
        }
        if (repository.exists(user.getId())) {
            throw new IllegalArgumentException("이미 존재하고 있는 유저입니다.");
        }
        repository.create(user);
    }


    @Override
    public User read(UUID id) {

        if (id == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }

        User user = repository.read(id);

        if (user == null) {
            throw new IllegalArgumentException("유저 정보가 없습니다.");
        }

        return user;
    }

    @Override
    public List<User> readAll() {
        return repository.readAll();
    }

    @Override
    public void update(UUID id, User user) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }

        if (user == null) {
            throw new IllegalArgumentException("수정할 유저 정보가 없습니다.");
        }

        if (!repository.exists(id)) {
            throw new IllegalArgumentException("수정할 유저가 존재하지 않습니다.");
        }
        repository.update(id, user);
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }

        if (!repository.exists(id)) {
            throw new IllegalArgumentException("삭제할 유저가 존재하지 않습니다.");
        }
        repository.delete(id);
    }
}

