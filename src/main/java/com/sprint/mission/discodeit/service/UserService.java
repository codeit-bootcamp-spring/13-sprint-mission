package com.sprint.mission.discodeit.service;


import com.sprint.mission.discodeit.dto.input.Login;
import com.sprint.mission.discodeit.dto.input.UserProfile;
import com.sprint.mission.discodeit.dto.output.UserState;


import java.util.List;
import java.util.UUID;

public interface UserService {
    void createUser(Login lgn, UserProfile upf);
    UserState getUserById(UUID id);
    List<UserState> getUserList();
    void updateProfileInfo(UUID id, String name, String pw);
    void updateProfileImage(UUID id, UserProfile upf);
    void deleteUser(UUID id);
}
