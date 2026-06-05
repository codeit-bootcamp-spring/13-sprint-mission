package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.UUID;

// User 엔티티(사용자)용 CRUD 기능 인터페이스
public interface UserService {
    void create(User user); // (C) 만들기 (사용자 생성)
    User read(UUID id); // (R) 한 명 조회 (아이디로 사용자 한 명 조회)
    List<User> readAll(); // (R) 모두 조회 (모든 사용자 리스트 조회)
    void update(User user); // (U) 수정 (사용자 정보 수정)
    void delete(UUID id); // (D) 삭제 (사용자 삭제)
}
