package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Message;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface MessageService {

  Message create(MessageCreateRequest messageCreateRequest,
                 List<BinaryContentCreateRequest> binaryContentCreateRequests);

  Message find(UUID messageId);

  // 기존 메서드 유지
  List<Message> findAllByChannelId(UUID channelId);

  // 페이지네이션 메서드 추가
  PageResponse<Message> findAllByChannelId(UUID channelId, Pageable pageable);

  Message update(UUID messageId, String newContent);

  void delete(UUID messageId);
}