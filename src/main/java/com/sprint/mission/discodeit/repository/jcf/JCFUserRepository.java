package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JCFUserRepository implements UserRepository {

    private final List<User> users = new ArrayList<>();

    @Override
    public User create(User newUser) {
        users.add(newUser);
        return newUser;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }
    @Override
    public List<User> findAll() {
        return users;
    }

    @Override
    public void update(User inputUser) {
        findById(inputUser.getId())
                .ifPresent(u -> u.updateName(inputUser));
    }

    @Override
    public void delete(UUID id) {
        users.removeIf(u -> u.getId().equals(id));
    }
}
/*
레포지토리 설계 및 구현
[ ] 다음의 조건을 만족하는 레포지토리 인터페이스의 구현체를 작성하세요.
[ ] 기존에 구현한 JCF*Service 구현체의 "저장 로직"과 관련된 코드를 참고하여 구현하세요.
 */