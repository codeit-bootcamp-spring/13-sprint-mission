package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;

import java.util.*;

public class JCFUserService implements UserService {

    private final Map<UUID, User> data;

    public JCFUserService() {
        this.data = new HashMap<>();
    }

    @Override
    public User createUser(String name, String email, String password) {
        User user = new User(name, email, password);
        this.data.put(user.getId(), user);
        return user;
    }

    @Override
    public User findById(UUID id) {
        return this.data.get(id);
    }

    @Override
    public List<User> findAll() {
        List<User> userArrayList = new ArrayList<>(data.values());
        return userArrayList;
    }

    @Override
    public User updateUser(UUID id, String name, String email, String password) {
        User updateUser = this.data.get(id);

        updateUser.updateName(name);
        updateUser.updateEmail(email);
        updateUser.updatePassword(password);
        return updateUser;
    }

    @Override
    public void delete(UUID id) {
        this.data.remove(id);
    }
}
