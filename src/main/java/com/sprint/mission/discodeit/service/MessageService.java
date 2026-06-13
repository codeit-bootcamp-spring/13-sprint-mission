package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    //(C)생성
    MessageResponse create(MessageCreateRequest request);
    //(R)조회 단건
    MessageResponse findById(UUID messageId);
    //(R)조회 다수[특정 채널 메시지 조회]
    List<MessageResponse> findAllByChannelId(UUID channelId);
    //(U)수정
    MessageResponse updateMessage(UUID messageId, MessageUpdateRequest request);
    //(D)삭제
    void delete(UUID messageId);
}
