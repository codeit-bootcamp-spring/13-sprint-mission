package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface UserStatusRepository {


     UserStatus findByUserId(UUID id);

    void create(UserStatus userStatus);

    UserStatus find(UUID id);

    List<UserStatus> findAll();

    void delete(UUID id);

    void update(UserStatus userStatus);
}
