package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageUpdateResponse;
import com.sprint.mission.discodeit.entity.Message;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    Message createMessage(MessageCreateRequest request, List<MultipartFile> files);
    List<Message> findAllByChannelId(UUID channelId);
    Message updateMessage(UUID messageId, MessageUpdateRequest request, List<MultipartFile> files);
    void deleteMessage(UUID messageId);

}
