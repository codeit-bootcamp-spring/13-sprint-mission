package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.*;
import com.sprint.mission.discodeit.dto.command.channel.ChannelUpdateCommand;
import com.sprint.mission.discodeit.dto.command.channel.PrivateChannelCommand;
import com.sprint.mission.discodeit.dto.command.channel.PublicChannelCommand;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    //PUBLIC 채널 생성
    ChannelDto createPublicChannel(PublicChannelCommand command);
    //PRIVATE 채널 생성
    ChannelDto createPrivateChannel(PrivateChannelCommand command);

    //채널단건 조회
    ChannelDto findByChannelId(UUID channelId);

    //사용자 전체 채널 조회
    List<ChannelDto> findAllByUserId(UUID id);

    //수정
    ChannelDto updateChannel(UUID channelId, ChannelUpdateCommand command);

    //삭제
    void deleteChannel(UUID channelId);
}
