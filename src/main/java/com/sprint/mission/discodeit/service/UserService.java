package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.UUID;

public interface UserService {
    void create(User user); // (C) 만들기
    User read(UUID id); // (R) 한 명 조회
    List<User> readAll(); // (R) 모두 조회
    void update(User user); // (U) 수정
    void delete(UUID id); // (D) 삭제
}
