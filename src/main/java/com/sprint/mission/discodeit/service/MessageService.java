package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.MessageUpdateResponse;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    MessageDto createMessage(MessageCreateRequest request, List<MultipartFile> files);
    PageResponse<MessageDto> findAllByChannelId(UUID channelId, Pageable pageable);
    MessageDto updateMessage(UUID messageId, MessageUpdateRequest request);
    void deleteMessage(UUID messageId);

}
