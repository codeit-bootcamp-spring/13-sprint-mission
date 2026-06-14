package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.dto.request.ChannelPrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelPublicRequest;
import com.sprint.mission.discodeit.dto.request.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class JCFChannelService implements ChannelService {

    private final List<Channel> data;

    public JCFChannelService() {
        this.data = new ArrayList<>();
    }

//    public Channel create(Channel channel) { // 매개변수 선언, 유저 생성하는 기능 구현
//        data.add(channel); // 창고에 넣기 (진짜 등록)
//        return channel;
//    }

    @Override
    public ChannelResponse createPrivateChannel(ChannelPrivateRequest dto) {
        return new ChannelResponse(UUID.randomUUID(), "PRIVATE 채널", "설명");
    }

    @Override
    public ChannelResponse createPublicChannel(ChannelPublicRequest dto) {
        return new ChannelResponse(UUID.randomUUID(), dto.name(), dto.description());
    }

    @Override
    public Optional<ChannelResponse> findById(UUID id) { // 단건 조회
        for (Channel foundChannel : data) {
            if (foundChannel.getId().equals(id)) {
                return Optional.of(new ChannelResponse(foundChannel.getId(), foundChannel.getChannelTitles(), foundChannel.getDescription()));
            }
        }
        return null;
    }

    @Override
    public List<ChannelResponse> findAll(UUID userId) { // 전체 조회
        return data.stream()
                .map(channel -> new ChannelResponse(channel.getId(),channel.getChannelTitles(), channel.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) { // 전체 조회
        return data.stream()
                .map(channel -> new ChannelResponse(channel.getId(),channel.getChannelTitles(), channel.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    public ChannelResponse update(UUID uuid, ChannelPublicRequest dto) {
        Channel foundChannel = null;
        for (Channel channel : data) {
            if (channel.getId().equals(uuid)) {
                foundChannel = channel;
                break;
            }
        }
        return new ChannelResponse(uuid, dto.name(), dto.description());
    }

    @Override
    public void delete(UUID id) {
        Channel foundChannel = null;
        for (Channel channel : data) {
            if (channel.getId().equals(id)) {
                foundChannel = channel;
                break;
            }
        }
        if (foundChannel != null) {
            data.remove(foundChannel);
        }
    }
}
