package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFUserRepository implements UserRepository {
    private final List<User> data = new ArrayList<>();


    @Override
    public void save(User user) {
        data.add(user);
    }

    @Override
    public User findById(UUID id) {
        for(User user : data) {
        if(user.getId().equals(id)){
            return user;
        }
        } return null;
    }

    @Override
    public List<User> findAll() {
        return data;
    }

    @Override
    public void delete(UUID id) {
        User foundUser = findById(id);
        if(foundUser != null){
            data.remove(foundUser);
        }

    }
}
