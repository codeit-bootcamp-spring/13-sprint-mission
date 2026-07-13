package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Message;
import java.util.List;
import java.util.UUID;

//Message 엔티티(메시지)용 CRUD 기능 인터페이스
public interface MessageService {

  MessageDto create(MessageCreateRequest messageCreateRequest,
      List<BinaryContentCreateRequest> binaryContentCreateRequests); // (C) 메시지 생성

  Message find(UUID messageId); // (R) 아이디로 메시지 한 개 조회

  PageResponse<MessageDto> findAllByChannelId(UUID channelId); // (R) 모든 메시지 리스트 조회

  MessageDto update(UUID messageId, MessageUpdateRequest request); //(U) 메시지 내용 수정

  void delete(UUID messageId); // (D) 메시지 삭제
}

