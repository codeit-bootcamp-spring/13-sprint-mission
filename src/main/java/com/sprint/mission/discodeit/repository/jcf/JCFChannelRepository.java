package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import lombok.NoArgsConstructor;

import java.util.*;

@NoArgsConstructor
public class JCFChannelRepository implements ChannelRepository {

    private static final Map<UUID, Channel> data=new HashMap<>(); // 인스턴스를 생성할 때 마다 새로운 Map을 만들지 않도록 한다

    @Override
    public Channel save(Channel channel) {
        data.put(channel.getId(), channel);
        return channel;
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<Channel> findAll() {
        return data.values().stream().toList();
    }

    @Override
    public void deleteById(UUID id) {
        data.remove(id);
    }

    @Override
    public boolean existById(UUID id) {
        return data.containsKey(id);
    }

}
