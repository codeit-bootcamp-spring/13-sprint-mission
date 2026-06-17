//package com.sprint.mission.discodeit.service.file;
//
//import com.sprint.mission.discodeit.entity.*;
//import com.sprint.mission.discodeit.repository.*;
//import com.sprint.mission.discodeit.service.*;
//import org.springframework.stereotype.*;
//
//import java.io.*;
//import java.nio.file.*;
//import java.util.*;
//
//
//public class FileMessageService implements MessageService{
//
//    private final MessageRepository repository;
//    private final UserService userService;
//    private final ChannelService channelService;
//
//    public FileMessageService(
//            MessageRepository repository,
//            UserService userService,
//            ChannelService channelService
//    ) {
//        this.repository = repository;
//        this.userService = userService;
//        this.channelService = channelService;
//    }
//
//    @Override
//    public Message read(UUID id) {
//        if (id == null) {
//            throw new IllegalArgumentException("메세지 ID가 없습니다.");
//        }
//        Message message = repository.find(id);
//        if (message == null) {
//            throw new IllegalArgumentException("존재하지 않는 메세지 ID입니다.");
//        }
//        return message;
//    }
//
//    @Override
//    public Message create(String content, UUID channelId, UUID authorId) {
//
//        userService.read(authorId);
//        channelService.read(channelId);
//
//        Message message =
//                new Message(content, channelId, authorId);
//
//        repository.create(message);
//
//        return message;
//    }
//
//    @Override
//    public List<Message> readAll() {
//        return repository.findAll();
//    }
//
//    @Override
//    public Message update(UUID id, String content) {
//        if (id == null) {
//            throw new IllegalArgumentException("메세지 ID는 필수입니다.");
//        }
//
//        if (!repository.exists(id)) {
//            throw new IllegalArgumentException("존재하지 않는 메세지 ID입니다.");
//        }
//
//        Message message = repository.find(id);
//        message.updateContent(content);
//
//        repository.update(id, message);
//
//        return message;
//    }
//
//    @Override
//    public void delete(UUID id) {
//        if (id == null) {
//            throw new IllegalArgumentException("메세지 ID는 필수입니다.");
//        }
//        if (!repository.exists(id)) {
//            throw new IllegalArgumentException("존재하지 않는 메세지 ID입니다.");
//        }
//        repository.delete(id);
//    }
//}