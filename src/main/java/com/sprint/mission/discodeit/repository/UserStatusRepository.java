package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

//userStatus 엔티티를 저장하고 조회하기 위한 Repository 인터페이스
public interface UserStatusRepository {
    UserStatus save(UserStatus userStatus); //userStatus 저장
    Optional<UserStatus> findById(UUID id); //userStatus Id를 이용하여 조회
    Optional<UserStatus> findByUserId (UUID userId); //사용자 ID를 이용하여 조회
    List<UserStatus> findAll(); //전체 userStatus 조회
    boolean existsById(UUID id); //특정 userStatus가 존재 여부 확인
    void deleteById(UUID id); //userStatus ID(PK)를 이용하여 삭제
    void deleteByUserId(UUID userId); //사용자 ID를 이용하여 삭제

}
