package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.UUID;
//사용자 상태(userStatus) 관련 비즈니스 로직을 정의하는 서비스 인터페이스
public interface UserStatusService {
    UserStatus create(UserStatusCreateRequest request); //사용자 상태 생성
    UserStatus find(UUID userStatusId); //사용자 상태 단건 조회
    List<UserStatus> findAll(); //전체 사용자 상태 조회
    UserStatus update(UUID userStatusId, UserStatusUpdateRequest request); //userStatus ID를 이용한 상태 수정
    UserStatus updateByUserId(UUID userId, UserStatusUpdateRequest request); //사용자 ID를 이용한 상태 수정
    void delete(UUID userStatusId); //사용자 상태 삭제
}
