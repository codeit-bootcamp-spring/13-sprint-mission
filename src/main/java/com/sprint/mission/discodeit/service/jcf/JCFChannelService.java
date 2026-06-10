package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.*;

//ChannelService를 실제로 동작시키는 JCF(컬렉션) 기반 구현체
public class JCFChannelService implements ChannelService {

    private final Map<UUID, Channel> data; //채널 데이터를 저장하는 메모리 저장소

    public JCFChannelService() {this.data = new HashMap<>();}

    @Override //채널 생성
    public Channel create(ChannelType type, String name, String description) {
        Channel channel = new Channel(type, name, description);
        this.data.put(channel.getId(), channel);
        return channel;
    }

    @Override //채널 단건조회
    public Channel find(UUID ChannelId) { //값이 존재하면 반환하고 값이 없으면 예외 발생
        Channel channel = this.data.get(ChannelId);
        return Optional.ofNullable(channel)
                .orElseThrow(()-> new NoSuchElementException("Channel with id " +  ChannelId + " not found"));
    }

    @Override //전체 채널 조횓
    public List<Channel> findAll() {
        return this.data.values().stream().toList();
    }

    @Override //채널 수정
    public Channel update(UUID channelId, String newName, String newDescription) {
        Channel channelNullable = this.data.get(channelId);
        Channel channel = Optional.ofNullable(channelNullable)
                .orElseThrow(()-> new NoSuchElementException("Channel with id " +  channelId + " not found"));
        channel.update(newName,newDescription);

        return channel;
    }

    @Override //채널 삭제
    public void delete(UUID ChannelId) {
        if (!this.data.containsKey(ChannelId)) {
            throw new NoSuchElementException("Channel with id " +  ChannelId + " not found");
        }
        this.data.remove(ChannelId);
    }
}
