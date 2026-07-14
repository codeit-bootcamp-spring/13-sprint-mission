package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelRepository extends JpaRepository<Channel, UUID> {

//    boolean existsChannelById(UUID channelId);
//    void createChannel(Channel channel);
//    Optional<Channel> findChannelById(UUID channelId);
//    List<Channel> findAllChannelsByChannelType(ChannelType channelType);
//    void save();
//    void deleteChannel(UUID channelId);

    List<Channel> findAllByType(ChannelType type);

}
