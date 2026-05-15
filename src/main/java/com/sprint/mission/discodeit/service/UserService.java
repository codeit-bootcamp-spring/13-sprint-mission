package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.*;

import java.util.*;

// 생성(Create) / 조회(Read) / 전체조회(Read All) / 수정(Update) / 삭제(Delete)

public interface UserService {

    void create(User user);

    User read(UUID id);

    List<User> readAll();

    void update(UUID id, User user);

    void delete(UUID id);
}
