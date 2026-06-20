package com.sprint.mission.discodeit.repository.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Repository
public class FileChannelRepository implements ChannelRepository {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String filePath = "channels.json";

    @Override
    public void save(Channel channel) {
        List<Channel> channels = findAll();
        channels.add(channel);
        saveAll(channels);
    }

    @Override
    public List<Channel> findAll() {
        File file = new File(filePath);
        if (!file.exists()) return new ArrayList<>();
        try {
            return objectMapper.readValue(file,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Channel.class));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void saveAll(List<Channel> channels) {
        try {
            objectMapper.writeValue(new File(filePath), channels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public Optional<Channel> findById(String id) {
        return findAll().stream()
                .filter(c -> id.equals(c.getId()))
                .findFirst();
    }


    @Override
    public void update(Channel channel) {
        List<Channel> channels = findAll();
        List<Channel> updatedChannels = channels.stream()
                .map(c -> channel.getId().equals(c.getId()) ? channel : c)
                .toList();
        saveAll(updatedChannels);
    }



    @Override
    public void delete(String id) {
        List<Channel> channels = findAll();
        boolean removed = channels.removeIf(channel -> id.equals(channel.getId()));

        if (removed) {
            saveAll(channels);
        }
    }
}