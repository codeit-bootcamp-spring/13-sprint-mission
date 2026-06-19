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


    // findById 수정(멘토님 코드리뷰)
    @Override
    public Optional<Channel> findById(String id) {
        return findAll().stream()
                .filter(c -> id.equals(c.getId())) // c.getId().equals(id) 에서 변경
                .findFirst();
    }


    // stream.map.filter로 표현(멘토님 코드리뷰)
    @Override
    public void update(Channel channel) {
        List<Channel> channels = findAll();
        // 스트림을 사용해 조건에 맞는 데이터만 변경 후 다시 리스트로 수집
        List<Channel> updatedChannels = channels.stream()
                .map(c -> channel.getId().equals(c.getId()) ? channel : c)
                .toList();
        saveAll(updatedChannels);
    }



    // delete 수정(멘토님 코드리뷰)
    @Override
    public void delete(String id) {
        List<Channel> channels = findAll();
        // channel.getId().equals(id) 에서 변경
        boolean removed = channels.removeIf(channel -> id.equals(channel.getId()));

        if (removed) {
            saveAll(channels);
        }
    }
}