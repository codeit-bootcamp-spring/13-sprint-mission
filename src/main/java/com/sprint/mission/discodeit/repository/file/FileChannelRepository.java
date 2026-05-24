package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileChannelRepository implements ChannelRepository {

    //channels 저장 객체 및 저장 경로
    private final List<Channel> channels = new ArrayList<>();
    private final Path binPath = Path.of("data/channels.ser");

    //ctor
    public FileChannelRepository() {
        File file = new File("data/channels.ser");
        // 파일 저장 위치에 파일이 존재한다면 로드하도록.
        if (file.exists())
            loadFromBinary();
    }

    //method
    //직렬화 메서드
    private void saveToBinary() {
        Path parent = binPath.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new RuntimeException("폴더 생성에 실패했습니다.");
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(binPath)))) {
            oos.writeObject(new ArrayList<>(channels));
        } catch (IOException e) {
            throw new RuntimeException("직렬화에 실패했습니다.");
        }
    }

    //역직렬화 메서드
    private void loadFromBinary() {
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(binPath)))) {
            List<Channel> channelsTemp = (List<Channel>) ois.readObject();
            // 일단 channels 초기화
            if (channels != null)
                channels.clear();
            // 유저에 역직렬화한 List<Channel> 넣기
            channels.addAll(channelsTemp);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("역직렬화에 실패했습니다.");
        } catch (IOException e) {
            throw new RuntimeException("역직렬화에 실패했습니다22.");
        }
    }


    //interface
    @Override
    public void save() {
        saveToBinary();
    }

    @Override
    public void createChannel(Channel channel) {
        channels.add(channel);
        saveToBinary();
    }

    @Override
    public Optional<Channel> findChannel(Channel channel) {
        if (channels.contains(channel)){
            return Optional.of(channel);
        }
        return Optional.empty();
    }

    @Override
    public List<Channel> findAll() {
        return channels;
    }

    @Override
    public void deleteChannel(Channel channel) {
        channels.remove(channel);
        saveToBinary();
    }
}
