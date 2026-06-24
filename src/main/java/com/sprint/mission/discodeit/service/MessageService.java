package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;


import java.util.List;
import java.util.UUID;

public interface MessageService {
    MessageResponse create(MessageCreateRequest request);
    MessageResponse find(UUID messageId); // 연관 도메인 별 아이디 구분 messageId, userId, channelId
    List<MessageResponse> findAllByChannelId(UUID channelId);
    MessageResponse update(UUID messageId, MessageUpdateRequest request); // 식별자인 id가 먼저 오는 것이 흐름상 자연스럽다
    void delete(UUID messageId);
}
