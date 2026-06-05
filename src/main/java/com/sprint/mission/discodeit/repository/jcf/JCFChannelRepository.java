package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//메모리(ArrayList)에 채널 정보를 저장하는 Repository
public class JCFChannelRepository implements ChannelRepository {
    //채널 저장 리스트
    private final List<Channel> channels = new ArrayList<>();

    //채널 저장
    public Channel save(Channel channel) {
        channels.add(channel);
        return channel;
    }

    //id로 채널 조회
    public Channel findById(UUID id) {
        for (Channel c : channels) {
            if (c.getId().equals(id))
                return c;
        }
        return null;
    }

    @Override
    public List<Channel> findAll() {
        return new ArrayList<>(channels);
    }

    @Override
    public void delete(UUID id) {

        channels.removeIf(
                message -> message.getId().equals(id)
        );
    }
}
