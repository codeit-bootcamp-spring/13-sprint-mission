package com.sprint.mission.discodeit.service;


import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;

import java.util.*;

public interface UserService {

    UserResponse create(UserRequest.CreateUserRequest request, CreateBinaryContentRequest profileImage);

    UserResponse find(UUID id);

    List<UserResponse> findAll();

    UserResponse update(UUID id,UserRequest.UpdateUserRequest request,CreateBinaryContentRequest profileImage);

    void delete(UUID id);

}
