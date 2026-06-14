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


//    @Override
//    public Channel create(Channel newChannel) {
//        List<Channel> channels = readFile();
//        channels.add(newChannel);
//        saveFile(channels);
//        return newChannel;
//    }

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
                foundChannel.updateTitles(new Channel(dto.name(),  dto.description()));
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
/*
기본 요구사항
File IO를 통한 데이터 영속화
[ ]  JCF 대신 FileIO와 객체 직렬화를 활용해 메소드를 구현하세요.
서비스 구현체 분석
[ ] JCF*Service 구현체와 File*Service 구현체를 비교하여 공통점과 차이점을 발견해보세요.
[ ] "비즈니스 로직"과 관련된 코드를 식별해보세요.
[ ] "저장 로직"과 관련된 코드를 식별해보세요.
 */