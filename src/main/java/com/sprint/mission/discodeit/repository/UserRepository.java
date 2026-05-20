package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.jcf.JCFSelectFilter;

import java.util.ArrayList;
import java.util.UUID;

public interface UserRepository {

    void create(String name, String id, String pw);
    ArrayList<User> select(JCFSelectFilter fn);
    void update(UUID id, String name, String userID, String pw);
    void delete(UUID id);
}
