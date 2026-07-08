package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    //(C)생성
    MessageDto create(MessageCreateRequest request, List<BinaryContentCreateRequest> attachments);
    //(R)조회
    MessageDto findById(UUID messageId);
    //(R)조회 다수[특정 채널 메시지 조회]
    PageResponse<MessageDto> findAllByChannelId(UUID channelId, Pageable pageable);
    //(U)수정
    MessageDto updateMessage(UUID messageId, MessageUpdateRequest request);
    //(D)삭제
    void delete(UUID messageId);
}
