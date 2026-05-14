package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.UUID;


public interface UserService {



    //(C)생성
    User createUser(String name, String email, String password);
    //(R)조회 단건
    User findById(UUID id);
    //(R)조회 다수
    List<User> findAll();
    //(U)수정
    User updateUser(UUID id, String name, String email, String password);
    //(D)삭제
    void delete(UUID id);
}
