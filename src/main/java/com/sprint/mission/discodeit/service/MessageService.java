package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface MessageService {

  MessageDto create(MessageCreateRequest messageCreateRequest,
      List<BinaryContentCreateRequest> binaryContentCreateRequests);

  MessageDto find(UUID messageId); // 연관 도메인 별 아이디 구분 messageId, userId, channelId

  PageResponse<MessageDto> findAllByChannelId(UUID channelId,
      Pageable pageable); // Pageable 파라미터로 변경해 페이지네이션 쉽게 적용 가능하도록 한다 

  MessageDto update(UUID messageId, MessageUpdateRequest request); // 식별자인 id가 먼저 오는 것이 흐름상 자연스럽다

  void delete(UUID messageId);
}
