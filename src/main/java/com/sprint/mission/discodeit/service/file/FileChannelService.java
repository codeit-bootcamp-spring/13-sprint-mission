//package com.sprint.mission.discodeit.service.file;
//
//import com.sprint.mission.discodeit.entity.Channel;
//import com.sprint.mission.discodeit.entity.ChannelType;
//import com.sprint.mission.discodeit.service.ChannelService;
//
//import java.io.*;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.*;
//
//public class FileChannelService implements ChannelService {
//
//    private final Path filePath;
//
//    public FileChannelService(Path filePath){
//        this.filePath = filePath;
//        if (!Files.exists(filePath.getParent())){
//            try {
//                Files.createDirectories(filePath.getParent());
//            }catch (IOException e){
//                throw new RuntimeException("디렉토리 생성 실패");
//            }
//        }
//    }
//
//    //파일에 data 저장
//    private void saveToFile(Map<UUID, Channel >data){
//        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))){
//            oos.writeObject(data);
//        } catch (IOException e) {
//            throw new RuntimeException("파일 저장 실패!");
//        }
//    }
//
//    private Map<UUID, Channel> loadFromFile(){
//        if (!Files.exists(filePath)){
//            return new HashMap<>();
//        }
//        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toFile()))){
//            return (Map<UUID, Channel>) ois.readObject();
//        }catch (IOException | ClassNotFoundException e){
//            throw new RuntimeException("파일 불러오기 실패");
//        }
//    }
//
//    //공개채널 생성
//    @Override
//    public Channel createChannel(String name, String description) {
//        Map<UUID, Channel> data = loadFromFile();
//        validateChannel(data, name, description);
//        Channel channel = new Channel(name, description);
//        data.put(channel.getChannelId(), channel);
//        saveToFile(data);
//        return channel;
//    }
//
//    //비공개 채널 생성
//    @Override
//    public Channel createChannel(String name, String description, ChannelType channelType) {
//        Map<UUID, Channel> data = loadFromFile();
//        validateChannel(data, name, description);
//        Channel channel = new Channel(name, description, channelType);
//        data.put(channel.getChannelId(), channel);
//        saveToFile(data);
//        return channel;
//    }
//
//    //채널 단건 조회
//    @Override
//    public Channel findByChannel(UUID ChannelId) {
//        Map<UUID, Channel> data = loadFromFile();
//        Channel channel = data.get(ChannelId);
//        if (channel == null){
//            throw new NoSuchElementException("존재하지 않는 채널 입니다! 채널명을 다시 확인 해 주세요!");
//
//        }
//        return channel;
//    }
//
//    //채널 전체 조회
//    @Override
//    public List<Channel> findAllChannel() {
//        Map<UUID, Channel> data = loadFromFile();
//        return new ArrayList<>(data.values());
//    }
//    //비공개 채널 수정
//    @Override
//    public Channel updateChannel(UUID ChannelId, String name, String description, ChannelType channelType) {
//        Map<UUID, Channel> data = loadFromFile();
//        Channel updateChannel = data.get(ChannelId);
//        if (updateChannel == null) {
//            throw new NoSuchElementException("존재하지 않는 채널입니다.");
//        }
//        if (name != null && !name.isBlank()){
//            updateChannel.updateChannel(name);
//        }
//        if (description != null){
//            updateChannel.updateChannelDescription(description);
//        }
//        if (channelType != null){
//            updateChannel.updateIsChannelType(channelType);
//        }
//        saveToFile(data);
//        return updateChannel;
//    }
//
//    //채널 삭제
//    @Override
//    public void deleteChannel(UUID id) {
//        Map<UUID, Channel> data = loadFromFile();
//        Channel deleteChannel = data.get(id);
//        if (deleteChannel == null){
//            throw new NoSuchElementException("존재하지 않는 채널입니다.");
//        }
//        data.remove(id);
//        saveToFile(data);
//    }
//
//    //채널 유효성 검사
//    private void validateChannel(Map<UUID, Channel>data, String name, String description) {
//        if (name == null || name.isBlank()){
//            throw new IllegalArgumentException("채널이름을 입력해 주세요!");
//        }
//        if (description == null || description.isBlank()){
//            throw new IllegalArgumentException("채널 설명을 작성해 주세요!");
//        }
//        boolean duplicateCheck = data.values().stream()
//                .anyMatch(channel -> channel.getName().equals(name));
//        if (duplicateCheck){
//            throw new IllegalArgumentException("이미 동일한 채널명이 존재합니다.");
//        }
//    }
//}
