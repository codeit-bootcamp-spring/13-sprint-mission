package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.command.*;
import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;

import java.time.*;
import java.util.List;
import java.util.UUID;

public interface MessageService {

    MessageDto create(CreateMessageCommand command, List<CreateBinaryContentCommand> file);

    MessageDto find(UUID messageId);

    List<MessageDto> findAllByChannelId(UUID channelId);

    MessageDto update(UUID messageId, UpdateMessageCommand command);

    void delete(UUID messageId);

    PageResponse<MessageDto> getMessages(UUID channelId, LocalDateTime cursor);
}
