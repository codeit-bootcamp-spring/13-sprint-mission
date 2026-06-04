package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.*;

import java.util.*;

// 생성(Create) / 조회(Read) / 전체조회(Read All) / 수정(Update) / 삭제(Delete)

public interface UserService {

    User create(String userName, String email, String passWord);

    User read(UUID id);

    List<User> readAll();

    User update(UUID id, String userName, String email, String passWord);

    void delete(UUID id);



}
