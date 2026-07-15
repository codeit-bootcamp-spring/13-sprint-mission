package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    MessageResponse create(MessageRequest.Create request,
                           List<CreateBinaryContentRequest> createBinaryContentRequests);

    MessageResponse find(UUID messageId);

    List<MessageResponse> findAllByChannelId(UUID channelId);

    MessageResponse update(UUID messageId, MessageRequest.Update request);

    void delete(UUID messageId);

    PageResponse<MessageResponse> getMessages(UUID channelId, int page);
}
