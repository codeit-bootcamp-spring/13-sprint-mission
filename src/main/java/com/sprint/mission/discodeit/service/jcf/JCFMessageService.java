package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFMessageRepository;
import com.sprint.mission.discodeit.service.MessageService;


import java.io.IOException;
import java.util.*;

public class JCFMessageService implements MessageService {

    // User: UUID id, Long createdAt, updatedAt, String username, email
    // Channel: UUID id, Long createdAt, updatedAt, ChannelType type, String name
    // Message: UUID id, Long createdAt, updatedAt, String content, UUID channelId

    // private final Map<UUID, Message> data=new HashMap<>();

    private final MessageRepository messageRepository=new JCFMessageRepository();

    @Override
    public Message createMessage(UUID channelId, String content, Long createdAt) throws IOException {
        Message message =new Message(channelId, content, createdAt);

        // data.put(message.getId(), message);
        return messageRepository.saveMessage(message);
    }

    @Override
    public Optional<Message> readMessage(UUID id) throws IOException { // 단건, 다건
        return messageRepository.findMessage(id);
        //return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<Message> readMessages() throws IOException {
        return messageRepository.findMessages();
        //return new ArrayList<>(data.values());
    }

    @Override
    public Message editMessage(UUID id, UUID newChannelId, String newContent, Long updatedAt) throws IOException {
        //Message message=data.get(id);
        Message message=messageRepository.findMessage(id)
                .orElseThrow();
        message.updateMessage(newChannelId, newContent, updatedAt);

        return message;
    }

    @Override
    public void deleteMessage(UUID id) throws IOException {
        messageRepository.deleteMessage(id);
        //data.remove(id);
    }



}
