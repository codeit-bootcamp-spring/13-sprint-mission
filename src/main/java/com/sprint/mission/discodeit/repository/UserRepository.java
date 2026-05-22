package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Predicate;

public interface UserRepository {

    void create(String name, String id, String pw);
    ArrayList<User> select(Predicate<User> fn);
    void update(UUID id, String name, String userID, String pw);
    void delete(UUID id);
}
