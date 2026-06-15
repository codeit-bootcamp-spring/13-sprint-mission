package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

//User 엔티티의 데이터 접근(data access) 기능을 정의하는 Repository 인터페이스
public interface UserRepository {
    User save(User user); //사용자 저장
    Optional<User> findById(UUID id); //Id를 이용하여 사용자 조회
    Optional<User> findByUsername(String username);
    List<User> findAll(); //전체 사용자 조회
    boolean existsById(UUID id); //사용자 존재 여부 확인
    void deleteById(UUID id); //사용자 삭제
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

}
