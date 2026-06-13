package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final ChannelService channelService;

    public FileMessageService(MessageRepository messageRepository, UserService userService, ChannelService channelService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.channelService = channelService;
    }

    @Override
    public Message create(UUID userId, UUID channelId, String content) {
        if (userService.read(userId) == null) {
            throw new IllegalArgumentException("존재하지 않는 계정입니다.");
        }
        if (channelService.read(channelId) == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        Message message = new Message(userId, channelId, content);
        messageRepository.create(message); // 저장소 위임
        System.out.println("메시지가 작성되었습니다!");
        return message;
    }

    @Override
    public Message read(UUID id) {
        Message message = messageRepository.read(id);
        if (message == null) {
            throw new IllegalArgumentException("존재하지 않는 메시지입니다.");
        }
        return message;
    }

    @Override
    public List<Message> readAll() {
        return messageRepository.readAll();
    }

    @Override
    public void update(UUID id, String content) {
        Message message = messageRepository.read(id);
        if (message != null) {
            message.update(content);
            messageRepository.update(message);
        } else {
            System.out.println("수정할 메시지가 존재하지 않습니다.");
        }
    }

    @Override
    public void delete(UUID id) {
        messageRepository.delete(id);
    }
}
