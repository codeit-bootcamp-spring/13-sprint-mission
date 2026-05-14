package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    //(C)생성
    Channel createChannel(String name); //PUBLIC
    Channel createChannel(String name, ChannelType channelType); //PUBLIC PRIVATE
    //(R)조회 단건
    Channel findById(UUID id);
    //(R)조회 다수
    List<Channel> findAll();
    //(U)수정
    Channel updateChannel(UUID id, String name, ChannelType channelType);
    Channel updateChannel(UUID id, String name);
    //(D)삭제
    void deleteChannel(UUID id);
}
