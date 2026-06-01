package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.io.*;
import java.util.List;
import java.util.UUID;

public class FileChannelRepository implements ChannelRepository {

    private ChannelRepository channelRepository;

    public FileChannelRepository() {
        this.channelRepository = new FileChannelRepository();
    }

    @Override
    public Channel create(Channel channel) {
        return channelRepository.create(channel);
    }

    @Override
    public Channel findById(UUID id) {
        return channelRepository.findById(id);
    }

    @Override
    public List<Channel> findAll() {
        return channelRepository.findAll();
    }

    @Override
    public void update(Channel channel) {
        channelRepository.update(channel);
    }

    @Override
    public void delete(UUID id) {
        channelRepository.delete(id);
    }
}
/*
레포지토리 설계 및 구현
[ ] 다음의 조건을 만족하는 레포지토리 인터페이스의 구현체를 작성하세요.
[ ] 기존에 구현한 File*Service 구현체의 "저장 로직"과 관련된 코드를 참고하여 구현하세요.
 */