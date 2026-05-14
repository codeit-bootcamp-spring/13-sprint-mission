package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    //(C)생성
    Channel createChannel(String name, boolean channelType);
    //(R)조회 단건
    Channel findById(UUID id);
    //(R)조회 다수
    List<Channel> findAll();
    //(U)수정
    Channel updateChannel(UUID id, String name, boolean channelType);
    //(D)삭제
    void deleteChannel(UUID id);
}
