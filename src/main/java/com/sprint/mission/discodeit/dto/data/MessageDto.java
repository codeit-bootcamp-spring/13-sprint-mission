package com.sprint.mission.discodeit.dto.data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageDto(
    UUID id,
    Instant createdAt,
    Instant updatedAt,
    String content,
    UUID channelId,
    UserDto author,                     // 작성자 정보를 UserDto로 포함
    List<BinaryContentDto> attachments  // 첨부파일 정보를 BinaryContentDto 리스트로 포함
) {

}