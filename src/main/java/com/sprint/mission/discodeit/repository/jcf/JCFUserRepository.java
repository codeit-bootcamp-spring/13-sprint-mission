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
    public boolean existsUser(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void createUser(User user) {
        users.add(user);
    }

    @Override
    public Optional<User> findUser(User user) {
        if (users.contains(user)) {
            return Optional.of(user);
        }

        return Optional.empty();
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
