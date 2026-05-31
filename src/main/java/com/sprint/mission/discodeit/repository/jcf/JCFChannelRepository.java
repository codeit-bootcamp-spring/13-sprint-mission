package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFChannelRepository implements ChannelRepository {

    private final List<Channel> channels = new ArrayList<>();

    @Override
    public Channel create(Channel newChannel) {
        channels.add(newChannel);
        return newChannel;
    }

    @Override
    public Channel findById(UUID id) {
        return channels.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst().orElse(null);
    }

    @Override
    public List<Channel> findAll() {
        return new ArrayList<>(channels);
    }

    @Override
    public void update(Channel requestChannel) {
        for (Channel channel : channels) {
            if (channel.getId().equals(requestChannel.getId())) {
                channel.updateTitles(requestChannel);
                break;
            }
        }
    }

    @Override
    public void delete(UUID id) {
        channels.removeIf(c -> c.getId().equals(id));
    }
}
/*
레포지토리 설계 및 구현
[ ] 다음의 조건을 만족하는 레포지토리 인터페이스의 구현체를 작성하세요.
[ ] 기존에 구현한 JCF*Service 구현체의 "저장 로직"과 관련된 코드를 참고하여 구현하세요.
 */