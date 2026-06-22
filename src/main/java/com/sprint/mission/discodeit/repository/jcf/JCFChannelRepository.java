package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.util.*;

public class JCFChannelRepository implements ChannelRepository {


    private final List<Channel> data;

    public JCFChannelRepository() {
        this.data = new ArrayList<>();
    }

    @Override
    public void save(Channel channel) {
        data.add(channel);
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        for (Channel ch : data) {
            if (ch.getId().equals(id)) {
                return Optional.of(ch);
            }
        }

        return Optional.empty();
    }



    @Override
    public List<Channel> findAll() {
        if (!data.isEmpty()) {
            return data;
        }
        return Collections.emptyList();
    }

    @Override
    public void delete(UUID id) {
        for (Channel channel : data) {
            if (channel.getId().equals(id)) {
                data.remove(channel);
                return;
            }
        }
        throw new IllegalArgumentException("채널을 찾을 수 없습니다.");
    }
}
