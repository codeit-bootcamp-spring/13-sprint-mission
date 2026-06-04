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
    public Channel createChannel(ChannelType type, String name, Long createdAt) throws IOException {
        Channel channel=new Channel(type, name, createdAt);

        return channelRepository.saveChannel(channel);

    }

    @Override
    public Optional<Channel> readChannel(UUID id) throws IOException {
        return channelRepository.fineChannel(id);
    }



    @Override
    public List<Channel> readChannels() throws IOException {

        return channelRepository.findChannels();
    }

    @Override
    public Channel editChannel(UUID id, ChannelType newType, String newName, Long updatedAt) throws IOException {

        Channel channel=channelRepository.fineChannel(id)
                .orElseThrow();
        channel.updateChannel(newType,newName,updatedAt);
        channelRepository.saveChannel(channel);
        return channel;
    }

    @Override
    public void deleteChannel(UUID id) throws IOException {

        channelRepository.deleteChannel(id); // 삭제는 리턴할 값 없음
    }

}


