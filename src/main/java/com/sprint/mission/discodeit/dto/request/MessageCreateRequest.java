package com.sprint.mission.discodeit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MessageCreateRequest {

    private String content;
    private UUID channelId;
    private UUID authorId;
    private List<String> fileNames;
    private List<String> contentTypes;
    private List<byte[]> dataList;
}