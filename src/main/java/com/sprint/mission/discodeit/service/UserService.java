package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;

import java.util.*;

// 생성(Create) / 조회(Read) / 전체조회(Read All) / 수정(Update) / 삭제(Delete)

public interface UserService {

    UserResponse create(UserRequest.CreateUserRequest request);

    UserResponse find(UUID id);

    List<UserResponse> findAll();

    UserResponse update(UUID id,UserRequest.UpdateUserRequest request);

    void delete(UUID id);



}
