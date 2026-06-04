package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public interface UserRepository {

    void create(User user);

    User read(UUID id);

    List<User> readAll();

    void update(UUID id, User user);

    void delete(UUID id);

    boolean exists(UUID id);

}
