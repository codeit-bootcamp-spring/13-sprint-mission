package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    User create(User user); // 유저 생성하는 기능 (추상 메서드 선언)

    User findById(UUID id);

    List<User> findAll();

    void update(User user);
    void delete(UUID id);
}

// [ ] 도메인 모델 별 CRUD(생성, 읽기, 모두 읽기, 수정, 삭제) 기능을 인터페이스로 선언하세요

/*
[ ] 등록 -> 사용자 등록
[ ] 조회(단건, 다건) -> 사용자 조회(특정 사용자 찾기/전체 목록 조회)
[ ] 수정 -> 사용자 정보(이름) 수정
[ ] 수정된 데이터 조회 -> 사용자 재검색
[ ] 삭제 -> 사용자 등록 삭제
[ ] 조회를 통해 삭제되었는지 확인 -> 사용자 재검색
 */