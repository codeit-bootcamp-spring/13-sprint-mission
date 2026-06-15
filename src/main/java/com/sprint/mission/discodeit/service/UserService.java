package com.sprint.mission.discodeit.service;


import com.sprint.mission.discodeit.dto.input.CreateUserInput;
import com.sprint.mission.discodeit.dto.input.UpdateUserInput;
import com.sprint.mission.discodeit.dto.output.BinaryObjectOutput;
import com.sprint.mission.discodeit.dto.output.UserOutput;


import java.util.List;
import java.util.UUID;

public interface UserService {
    void createUser(CreateUserInput upf);
    UserOutput getUserById(UUID id);
    BinaryObjectOutput getUserThumbnail(UUID id);
    List<UserOutput> getUserList();
    void update(UpdateUserInput uui);
    void delete(UUID id);
}
