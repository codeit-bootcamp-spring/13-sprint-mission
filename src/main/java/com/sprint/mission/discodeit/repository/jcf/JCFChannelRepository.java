package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.util.*;

//메모리(ArrayList)에 채널 정보를 저장하는 Repository
public class JCFChannelRepository implements ChannelRepository {
   private final Map<UUID, Channel> data;
   public JCFChannelRepository() {this.data = new HashMap<>();}

    @Override //채널 저장
    public Channel save(Channel channel) {
        this.data.put(channel.getId(), channel);
        return channel;
    }

    @Override //id로 채널 조회
    public Optional<Channel> findById(UUID id) {return Optional.ofNullable(this.data.get(id));}

    @Override
    public List<Channel> findAll() { return this.data.values().stream().toList();}

    @Override
    public boolean existsById(UUID id) {return this.data.containsKey(id);}

    @Override
    public void deleteById(UUID id) {this.data.remove(id);}
}
