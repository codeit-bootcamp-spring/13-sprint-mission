package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

public class JCFMessageService implements MessageService {


    private final Map<UUID, Message> data;
    private final UserService userService;
    private final ChannelService channelService;

    public JCFMessageService(UserService userService, ChannelService channelService){
        this.data = new HashMap<>();
        this.userService = userService;
        this.channelService = channelService;
    }

    @Override
    public Message createContent(String content, UUID channelId, UUID authorId) {
        channelService.findByChannel(channelId);
        userService.findByUser(authorId);
        if (content == null || content.isBlank()) {
            throw new NoSuchElementException("메시지를 입력해 주세요.");
        }

        Message message = new Message(content, channelId, authorId);
        data.put(message.getId(), message);
        return message;
    }

    @Override
    public Message findByMessage(UUID messageId) {
        Message message = data.get(messageId);
        if (message == null) {
            throw new NoSuchElementException("존재하지 않는 메시지 입니다.");
        }
        return message;
    }

    @Override
    public List<Message> findAllByMessage(UUID channelId) {
        return data.values().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .collect(Collectors.toList());
    }

    @Override
    public Message updateContent(UUID messageId, String content) {
        Message updateContent = data.get(messageId);
        if (updateContent == null) {
            throw new NoSuchElementException("존재하지 않는 메시지 입니다.");
        }
        updateContent.updateContent(content);
        return  updateContent;
    }

    @Override
    public void deleteMessage(UUID messageId) {
        Message message = data.get(messageId);
        if (message == null){
            throw  new NoSuchElementException("존재하지 않는 메시지 입니다.");
        }
        data.remove(messageId);
    }
}
