package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.dto.request.ChannelPrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelPublicRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class FileChannelService implements ChannelService {

    // 주소 설정, 변수를 대문자로
    private final Path DIRECTORY = Paths.get(System.getProperty("user.dir"), "data");
    private final Path filePath = DIRECTORY.resolve("channels.ser");

    public FileChannelService() {
        try {
            Files.createDirectories(DIRECTORY);
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
    public ChannelResponse createPrivateChannel(ChannelPrivateRequest dto) {
        return new ChannelResponse(UUID.randomUUID(), "PRIVATE 채널", "설명");
    }

    @Override
    public ChannelResponse createPublicChannel(ChannelPublicRequest dto) {
        return new ChannelResponse(UUID.randomUUID(), dto.name(), dto.description());
    }

    @Override
    public Optional<ChannelResponse> findById(UUID id) {
        List<Channel> channels = readFile();
        for (Channel channel : channels) {
            if (channel.getId().equals(id)) {
                return Optional.of(new ChannelResponse(channel.getId(), channel.getChannelTitles(),  channel.getDescription()));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<ChannelResponse> findAll(UUID userId) {
        return readFile().stream()
                .map(channel -> new ChannelResponse
                        (channel.getId(),channel.getChannelTitles(), channel.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        return readFile().stream()
                .map(channel -> new ChannelResponse
                        (channel.getId(),channel.getChannelTitles(), channel.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    public ChannelResponse update(UUID uuid, ChannelPublicRequest dto) {
        List<Channel> channels = readFile();
        for (Channel foundChannel : channels) {
            if (foundChannel.getId().equals(uuid)) {
                foundChannel.updateTitles(dto.name(),  dto.description());
                saveFile(channels);
                break;
            }
        }
        return new ChannelResponse(uuid, dto.name(), dto.description());
    }

    @Override
    public void delete(UUID id) {
        List<Channel> FoundChannel = readFile();
        FoundChannel.removeIf(c -> c.getId().equals(id));
        saveFile(FoundChannel);
    }
}