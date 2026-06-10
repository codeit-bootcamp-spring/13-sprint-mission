package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.UUID;

// User 엔티티(사용자)용 CRUD 기능 인터페이스
public interface UserService {
    User create(String username, String email, String password); // (C) 만들기 (사용자 생성)
    User find(UUID userId); // (R) 한 명 조회 (아이디로 사용자 한 명 조회)
    List<User> findAll(); // (R) 모두 조회 (모든 사용자 리스트 조회)
    User update(UUID userId, String newUsername, String newEmail, String newPassword); // (U) 수정 (사용자 정보 수정)
    void delete(UUID userId); // (D) 삭제 (사용자 삭제)
}

