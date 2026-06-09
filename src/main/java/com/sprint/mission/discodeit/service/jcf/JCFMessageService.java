package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

public class JCFMessageService implements MessageService {

    @Override
    public MessageResponse create(MessageCreateRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MessageResponse update(MessageUpdateRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException();
    }
}