package com.sprint.mission.discodeit.service.file;


import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FileChannelService implements ChannelService {


   private final ChannelRepository channelRepository=new FileChannelRepository();

    @Override
    public Channel createOne(ChannelType type, String name, Long createdAt) throws IOException {
        Channel channel=new Channel(type, name, createdAt);

        return channelRepository.createOne(channel);

    }

    @Override
    public Optional<Channel> readOne(UUID id) throws IOException {
        return channelRepository.readOne(id);
    }



    @Override
    public List<Channel> readAll() throws IOException {

        return channelRepository.readAll();
    }

    @Override
    public Channel editOne(UUID id, ChannelType newType, String newName, Long updatedAt) throws IOException {

        Channel channel=channelRepository.readOne(id)
                .orElseThrow();
        channel.updateChannel(id,newType,newName,updatedAt);
        channelRepository.createOne(channel);
        return channel;
    }

    @Override
    public void deleteOne(UUID id) throws IOException {

        channelRepository.deleteOne(id); // 삭제는 리턴할 값 없음
    }

}


