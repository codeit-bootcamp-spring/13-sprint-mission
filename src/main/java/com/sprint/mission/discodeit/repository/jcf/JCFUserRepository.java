package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JCFUserRepository implements UserRepository {

    //필드
    private final List<User> users = new ArrayList<>();

    //ctor
    public JCFUserRepository() {}

    //interface
    @Override
    public void save() {}

    @Override
    public boolean existsUserByEmail(String email) {
        return users.stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public void createUser(User user) {
        users.add(user);
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return users;
    }

    @Override
    public void deleteUser(User user) {
        users.remove(user);
    }
}
