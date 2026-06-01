package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.UUID;


public interface UserService {



    //(C)생성
    User createUser(String name, String email, String password);
    //(R)조회 단건
    User findByUser(UUID userId);
    //(R)조회 다수
    List<User> findAllUser();
    //(U)수정
    User updateUser(UUID userId, String name, String email, String password);
    //(D)삭제
    void deleteUser(UUID userId);
}
