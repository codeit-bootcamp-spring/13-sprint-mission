package com.sprint.mission.discodeit.service;


import com.sprint.mission.discodeit.dto.input.CreateUserInput;
import com.sprint.mission.discodeit.dto.output.BinaryObjectOutput;
import com.sprint.mission.discodeit.dto.output.UserOutput;


import java.util.List;
import java.util.UUID;

public interface UserService {
    void createUser(CreateUserInput upf);
    UserOutput getUserById(UUID id);
    BinaryObjectOutput getUserThumbnail(UUID id);
    List<UserOutput> getUserList();
    void updateProfileInfo(UUID id, String name, String pw);
    void updateProfileImage(UUID id, CreateUserInput upf);
    void deleteUser(UUID id);
}
