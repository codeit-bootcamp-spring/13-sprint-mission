package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    //(C)생성
    Channel createChannel(String name, String description); //PUBLIC
    Channel createChannel(String name, String description ,ChannelType channelType); //PUBLIC PRIVATE
    //(R)조회 단건
    Channel findByChannel(UUID ChannelId);
    //(R)조회 다수
    List<Channel> findAllChannel();
    //(U)수정
    Channel updateChannel(UUID ChannelId, String name, String description ,ChannelType channelType);
    Channel updateChannel(UUID ChannelId, String name, String description);
    //(D)삭제
    void deleteChannel(UUID id);
}
