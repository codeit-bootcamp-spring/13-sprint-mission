package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//채널 정보를 파일(channels.dat)에 저장하는 Repository
public class FileChannelRepository implements ChannelRepository {
    private final String filePath = "channels.dat"; //채널 저장 파일
    private List<Channel> channels = new ArrayList<>(); //메모리 채널 목록

    public FileChannelRepository() {
        load();
    } //생성 시 데이터 로드

    //채널 저장 후 파일 반영
    @Override
    public Channel save(Channel channel) {
        channels.add(channel);
        saveToFile();
        return channel;
    }

    //id로 채널 조회
    @Override
    public Channel findById(UUID id){
        for (Channel channel : channels) {
            if (channel.getId().equals(id))
                return channel;
        }
        return null;
    }

    //파일 저장
    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(channels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //파일로드
    @SuppressWarnings("unchecked")
    private void load() {
        File file = new File(filePath);
        if (!file.exists()) {
            channels = new ArrayList<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            channels = (List<Channel>) ois.readObject();
        } catch (Exception e) {
            channels = new ArrayList<>();
        }
    }

    @Override
    public List<Channel> findAll() {
        return new ArrayList<>(channels);
    }

    @Override
    public void delete(UUID id) {

        channels.removeIf(
                channel -> channel.getId().equals(id)
        );

        saveToFile();
    }}