package com.sprint.mission.discodeit.service.repository;

import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public interface UserRepository {

    public void save(User user);

    public User findById(UUID id);

    public List<User> findAll();

    public void deleteById(UUID id);

    public void update(User user);

}
