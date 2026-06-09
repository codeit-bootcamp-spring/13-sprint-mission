package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageService {

    //(C)생성
    Message create(MessageCreateRequest request);
    //(R)조회 단건
    Message findById(UUID messageId);
    //(R)조회 다수[특정 채널 메시지 조회]
    List<Message> findAllByChannelId(UUID channelId);
    //(U)수정
    Message update(UUID messageId, MessageUpdateRequest request);
    //(D)삭제
    void delete(UUID messageId);
}
