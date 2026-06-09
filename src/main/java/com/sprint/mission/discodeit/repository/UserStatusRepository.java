package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface UserStatusRepository {


    public UserStatus findByUserId(UUID id);

    void create(UserStatus userStatus);

}
