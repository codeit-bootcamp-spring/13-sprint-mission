package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.ChannelPrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelPublicRequest;
import com.sprint.mission.discodeit.dto.request.ChannelResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelService {

    // [요구사항] PRIVATE 채널과 PUBLIC 채널 생성 메소드 분리 및 DTO 그룹화
    ChannelResponse createPrivateChannel(ChannelPrivateRequest dto);
    ChannelResponse createPublicChannel(ChannelPublicRequest dto);

//    Channel create (Channel channel);

    Optional<ChannelResponse> findById(UUID id);
    List<ChannelResponse> findAll(UUID userId);
    List<ChannelResponse> findAllByUserId(UUID userId);
    ChannelResponse update(UUID id, ChannelPublicRequest dto);
    void delete(UUID id);
}