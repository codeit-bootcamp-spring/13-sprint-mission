package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// User 데이터를 저장하고 조회하기 위한 Spring Data JPA Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // 같은 username을 가진 사용자가 이미 존재하는지 확인
    boolean existsByUsername(String username);

    // 같은 email을 가진 사용자가 이미 존재하는지 확인
    boolean existsByEmail(String email);

    // username으로 사용자 조회
    User findByUsername(String username);

    // email로 사용자 조회
    User findByEmail(String email);
}