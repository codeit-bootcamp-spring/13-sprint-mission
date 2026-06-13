package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

//채널 관련 비즈니스 로직을 담당하는 Service 계층
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository; //채널 저장소 객체

    public BasicChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @Override //채널 생성
    public Channel create(ChannelType type, String name, String decription) {
        Channel channel = new Channel(type, name, decription);
        return channelRepository.save(channel);
    }

    @Override //채널 단건조회
    public Channel find(UUID channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new NoSuchElementException("Channel with id " + channelId + " not found"));
    }

    @Override //전체 채널 조회
    public List<Channel> findAll() {
        return channelRepository.findAll();
    }

    @Override //채널 수정
    public Channel update(UUID channelId, String newName, String newDecription) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NoSuchElementException("Channel with id " + channelId + " not found"));
        channel.update(newName, newDecription);
        return channelRepository.save(channel);
    }

    @Override //채널 삭제
    public void delete(UUID channelId) {
        if (!channelRepository.existsById(channelId)) {
            throw new NoSuchElementException("Channel with id " + channelId + " not found");
        }
        channelRepository.deleteById(channelId);
    }
}