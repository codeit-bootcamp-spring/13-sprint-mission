package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFChannelService implements ChannelService {

    private final List<Channel> data;

    public JCFChannelService() {
        this.data = new ArrayList<>();
    }

    public Channel create(Channel channel) { // 매개변수 선언, 유저 생성하는 기능 구현
        data.add(channel); // 창고에 넣기 (진짜 등록)
        return channel;
    }

    public Channel findById(UUID id) { // 단건 조회
        for (Channel foundChannel : data) {
            if (foundChannel.getId().equals(id)) {
                return foundChannel;
            }
        }
        return null;
    }

    public List<Channel> findAll() { // 전체 조회
        return data;
    }

    public void update(Channel requestChannel) {
        Channel foundChannel = findById(requestChannel.getId());
        if (foundChannel != null) {
            foundChannel.updateTitles(requestChannel);
        }
    }

    public void delete(UUID id) {
        Channel foundChannel = findById(id);
        if (foundChannel != null) {
            data.remove(foundChannel);
        }
    }
}
