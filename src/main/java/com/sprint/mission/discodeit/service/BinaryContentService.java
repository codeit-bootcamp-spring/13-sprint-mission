package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.UUID;

//바이너리 데이터(Binary content) 관리 서비스 인터페이스
public interface BinaryContentService {

  BinaryContent create(BinaryContentCreateRequest request); //새로운 바이너리 데이터를 생성(저장)함.

  BinaryContentDto find(UUID binaryContentId); //특정 바이너리 데이터를 조회함

  List<BinaryContentDto> findAllByIdIn(List<UUID> binaryContentIds); //여러개의 바이너리 데이터를 한 번에 조회함.

  void delete(UUID binaryContentId); //특정 바이너리 데이터를 삭제함.
}
