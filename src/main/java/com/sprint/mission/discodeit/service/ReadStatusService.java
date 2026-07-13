package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.ReadStatusDto;
import com.sprint.mission.discodeit.dto.request.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.readStatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;

//읽을 상태(ReadStatus) 관련 비즈니스 로직을 정의하는 서비스 인터페이스
public interface ReadStatusService {

  ReadStatusDto create(ReadStatusCreateRequest request); //읽을 상태 생성

  ReadStatus find(UUID readStatusId); //읽을 상태 단건 조회

  List<ReadStatusDto> findAllByUserId(UUID userId); //특정 사용자의 모든 읽은 상태 조회

  ReadStatusDto update(UUID readStatusId, ReadStatusUpdateRequest readStatus); //읽을 상태 수정

  void delete(UUID readStatusId); //읽을 상태 삭제
}
