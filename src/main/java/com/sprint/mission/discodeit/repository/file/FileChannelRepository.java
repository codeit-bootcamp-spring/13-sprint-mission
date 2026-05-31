package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileChannelRepository implements ChannelRepository {

    // 주소 설정
    private final Path directory = Paths.get(System.getProperty("channel.dir"), "data");
    private final Path filePath = directory.resolve("channels.ser");

    public FileChannelRepository() {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFile(List<Channel> channels) {
        try (ObjectOutputStream oos = new ObjectOutputStream
                (new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(channels);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private List<Channel> readFile() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }
            try (ObjectInputStream ois = new ObjectInputStream
                    (new FileInputStream(filePath.toFile()))) {
                return (List<Channel>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }


    @Override
    public Channel create(Channel newChannel) {
        List<Channel> channels = readFile();
        channels.add(newChannel);
        saveFile(channels);
        return newChannel;
    }

    @Override
    public Channel findById(UUID id) {
        List<Channel> channels = readFile();
        for (Channel channel : channels) {
            if (channel.getId().equals(id)) {
                return channel;
            }
        }
        return null;
    }

    @Override
    public List<Channel> findAll() {
        return readFile();
    }

    @Override
    public void update(Channel requestChannel) {
        List<Channel> channels = readFile();
        for (Channel foundChannel : channels) {
            if (foundChannel.getId().equals(requestChannel.getId())) {
                foundChannel.updateTitles(requestChannel);
                break;
            }
        }
    }

    @Override
    public void delete(UUID id) {
        List<Channel> FoundChannel = readFile();
        FoundChannel.removeIf(c -> c.getId().equals(id));
        saveFile(FoundChannel);
    }
}
/*
레포지토리 설계 및 구현
[ ] 다음의 조건을 만족하는 레포지토리 인터페이스의 구현체를 작성하세요.
[ ] 기존에 구현한 File*Service 구현체의 "저장 로직"과 관련된 코드를 참고하여 구현하세요.
 */