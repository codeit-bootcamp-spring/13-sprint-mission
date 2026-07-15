package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// UserStatus 데이터를 저장하고 조회하기 위한 Spring Data JPA Repository
public interface UserStatusRepository extends JpaRepository<UserStatus, UUID> {

    // userId로 UserStatus 조회
    // 특정 사용자의 상태 정보를 찾을 때 사용
    UserStatus findByUserId(UUID userId);

    // userId에 해당하는 UserStatus가 존재하는지 확인
    boolean existsByUserId(UUID userId);

    // userId로 UserStatus 삭제
    // 사용자가 삭제될 때 해당 사용자의 상태 정보도 같이 삭제할 때 사용
    void deleteByUserId(UUID userId);
}