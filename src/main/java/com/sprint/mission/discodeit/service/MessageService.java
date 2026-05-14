package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    //(C)생성
    Message createContent(String content, UUID channelId, UUID authorId);
    //(R)조회 단건
    Message findById(UUID id);
    //(R)조회 다수[특정 채널 메시지 조회]
    List<Message> findAllByChannelId(UUID channelId);
    //(U)수정
    Message updateMessage(UUID id, String content);
    //(D)삭제
    void deleteChannel(UUID id);
}
