package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStatusRepository {
    UserStatus save(UserService userService); //사용자 저장
    Optional<UserStatus> findById(UUID id); //Id를 이용하여 사용자 조회
    Optional<UserStatus> findByUserId (UUID userId);
    List<UserStatus> findAll(); //전체 사용자 조회
    boolean existsById(UUID id); //사용자 존재 여부 확인
    void deleteById(UUID id); //사용자 삭제
    void deleteByUserId(UUID userId);

}
