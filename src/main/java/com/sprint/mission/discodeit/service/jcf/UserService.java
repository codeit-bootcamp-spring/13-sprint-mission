package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.UUID;

public interface UserService {

    User create(String username, String email, String password, UserStatus userStatus);
    User read(UUID id);
    List<User> readAll();
    User update(UUID id,
                String username,
                String password,
                UserStatus userStatus);
    void delete(UUID id);


}
