package com.sprint.mission.discodeit.dto.message;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@ToString
@AllArgsConstructor
public class MessageCreateRequest {
    private String content;
    private UUID channelId;
    private UUID authorId;
    private List<BinaryContentCreateRequest> attachmentIds;

}
