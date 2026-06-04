package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileChannelService implements ChannelService {

    public static final Path CHANNEL_PATH = Paths.get("data/channels.ser");

    private final Map<UUID, Channel> channelMap;

    public FileChannelService() {
        this.channelMap = loadFromFile();
    }

    @Override
    public Channel create(Channel channel) {
        channelMap.put(channel.getId(), channel);
        saveToFile();
        return channel;
    }

    @Override
    public Channel read(UUID id) {
        Channel channel = channelMap.get(id);
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        return channel;
    }

    @Override
    public List<Channel> readAllChannels() {
        return new ArrayList<>(channelMap.values());
    }

    @Override
    public Channel update(UUID id, String channelName, String description, ChannelType channelType) {
        Channel channel = channelMap.get(id);
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        channel.updateChannelName(channelName);
        channel.updateDescription(description);
        channel.updateChannelType(channelType);
        saveToFile();
        return channel;
    }

    public Channel updateChannelName(UUID id, String channelName) {
        Channel channel = channelMap.get(id);
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        channel.updateChannelName(channelName);
        saveToFile();
        return channel;
    }

    public Channel updateDescription(UUID id, String description) {
        Channel channel = channelMap.get(id);
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        channel.updateDescription(description);
        saveToFile();
        return channel;
    }

    public Channel updateChannelType(UUID id, ChannelType channelType) {
        Channel channel = channelMap.get(id);
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        channel.updateChannelType(channelType);
        saveToFile();
        return channel;
    }


    // 채널에 유저 추가
    public Channel addMember(UUID channelId, User user) {
        Channel channel = channelMap.get(channelId);
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        if (channel.getMembers().contains(user)) {
            throw new IllegalArgumentException("이미 채널에 추가된 유저입니다.");
        }
        channel.addMember(user);
        saveToFile();
        return channel;
    }

    @Override
    public void delete(UUID id) {
        channelMap.remove(id);
        saveToFile();
    }

    private void saveToFile() {
        try {
            Files.createDirectories(CHANNEL_PATH.getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(CHANNEL_PATH)))) {
                oos.writeObject(new LinkedHashMap<>(channelMap));
            }
            System.out.println("채널 저장 완료: " + CHANNEL_PATH.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("채널 저장에 실패했습니다.", e);
        }
    }

    private Map<UUID, Channel> loadFromFile() {
        if (!Files.exists(CHANNEL_PATH)) {
            return new LinkedHashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(CHANNEL_PATH)))) {
            Map<UUID, Channel> map = (Map<UUID, Channel>) ois.readObject();
            System.out.println("채널 불러오기 완료: " + CHANNEL_PATH.toAbsolutePath());
            return map;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("채널 불러오기에 실패했습니다.", e);
        }
    }
}
