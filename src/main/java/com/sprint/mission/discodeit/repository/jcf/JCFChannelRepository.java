package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@NoArgsConstructor
public class JCFChannelRepository implements ChannelRepository {

    //필드
    private final List<Channel> channels = new ArrayList<>();

    //interface
    @Override
    public boolean existsChannelById(UUID channelId) {
        return channels.stream()
                .anyMatch(channel -> channel.getId().equals(channelId));
    }

    @Override
    public void createChannel(Channel channel) {
        channels.add(channel);
    }

    @Override
    public Optional<Channel> findChannelById(UUID channelId) {
        return channels.stream()
                .filter(channel -> channel.getId().equals(channelId))
                .findFirst();
    }

    @Override
    public List<Channel> findAllChannelsByChannelType(ChannelType channelType) {
        return channels.stream()
                .filter(channel -> channel.getType() == channelType)
                .toList();
    }

    @Override
    public void save() {

    }

    @Override
    public void deleteChannel(UUID channelId) {
        channels.remove(
                channels.stream()
                        .filter(channel -> channel.getId().equals(channelId))
                        .findFirst()
                        .get()
        );
    }
}
