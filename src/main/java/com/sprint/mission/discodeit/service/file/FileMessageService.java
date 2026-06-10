//package com.sprint.mission.discodeit.service.file;
//
//import com.sprint.mission.discodeit.entity.Message;
//import com.sprint.mission.discodeit.entity.User;
//import com.sprint.mission.discodeit.service.ChannelService;
//import com.sprint.mission.discodeit.service.MessageService;
//import com.sprint.mission.discodeit.service.UserService;
//
//import java.io.*;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class FileMessageService implements MessageService {
//
//    private UserService userService;
//    private ChannelService channelService;
//    private final Path filePath;
//
//
//
//
//    //디렉토리 생성
//    public FileMessageService(Path filePath, UserService userService, ChannelService channelService) {
//        this.filePath = filePath;
//        this.userService = userService;
//        this.channelService = channelService;
//
//        if (!Files.exists(filePath.getParent())) {
//            try {
//                Files.createDirectories(filePath.getParent());
//            } catch (IOException e){
//                throw  new RuntimeException("디렉토리 생성 실패");
//            }
//        }
//    }
//
//    // 파일에 data 저장하기
//    private void saveToFile(Map<UUID, Message>data){
//        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
//            oos.writeObject(data);
//        } catch (IOException e) {
//            throw new RuntimeException("파일 저장 실패!");
//        }
//    }
//
//    //파일에서 data 불러오기
//    private Map<UUID, Message> loadFromFile(){
//        if (!Files.exists(filePath)){
//            return new HashMap<>();
//        }
//        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toFile()))){
//            return (Map<UUID, Message>) ois.readObject();
//        }catch (IOException | ClassNotFoundException e){
//            throw new RuntimeException("파일 불러오기 실패");
//        }
//    }
//
//    //메시지 생성
//    @Override
//    public Message createContent(String content, UUID channelId, UUID authorId) {
//        Map<UUID, Message> data = loadFromFile();
//         channelService.findByChannel(channelId);
//         userService.findByUser(authorId);
//         if (content == null || content.isBlank()){
//             throw new IllegalArgumentException("메시지를 입력해 주세요!");
//         }
//         Message message = new Message(content, channelId, authorId);
//         data.put(message.getMessageId(), message);
//         saveToFile(data);
//         return message;
//    }
//
//    // 메세지 조회
//    @Override
//    public Message findByMessage(UUID messageId) {
//        Map<UUID, Message> data = loadFromFile();
//        Message message = data.get(messageId);
//        if (message == null){
//            throw new NoSuchElementException("존재하지 않는 메시지 입니다!");
//        }
//        return message;
//    }
//
//    //채널 메시지 전체 조회
//    @Override
//    public List<Message> findAllByMessage(UUID channelId) {
//        Map<UUID, Message> data = loadFromFile();
//        return data.values().stream()
//                .filter(message -> message.getChannelId().equals(channelId))
//                .collect(Collectors.toList());
//    }
//
//    //메시지 수정
//    @Override
//    public Message updateContent(UUID messageId, String content) {
//        Map<UUID, Message> data = loadFromFile();
//        Message updateContent = data.get(messageId);
//        if (updateContent == null){
//            throw new NoSuchElementException("존재하지 않는 메시지 입니다.");
//        }
//        updateContent.updateContent(content);
//        saveToFile(data);
//        return updateContent;
//    }
//
//    //메시지 삭제
//    @Override
//    public void deleteMessage(UUID messageId) {
//        Map<UUID, Message> data = loadFromFile();
//        Message deleteMessage = data.get(messageId);
//        if (deleteMessage == null){
//            throw new NoSuchElementException("존재하지 않는 메시지 입니다.");
//        }
//        data.remove(messageId);
//        saveToFile(data);
//    }
//}
