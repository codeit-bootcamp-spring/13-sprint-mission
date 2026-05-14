package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Message;

import java.util.*;

public class JCFMessageService implements MessageService {

    private final Map<UUID, Message> data;

    public JCFMessageService() {
        this.data = new HashMap<>();
    }

    @Override
    public Message createContent(String content, UUID channelId, UUID authorId) {
        Message message = new Message(content, channelId, authorId);
        this.data.put(message.getId(), message);
        return message;
    }

    @Override
    public Message findById(UUID id) {
        return this.data.get(id);
    }

    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        List<Message> messages = new ArrayList<>();
        for (Message message : this.data.values()) {
            if (message.getChannelId().equals(channelId)) {
                messages.add(message);
            }
        }
        return messages;
    }

    @Override
    public Message updateMessage(UUID id, String content) {
        Message updateContent = this.data.get(id);

        updateContent.updateContent(content);
        return updateContent;
    }

    @Override
    public void deleteChannel(UUID id) {
        this.data.remove(id);
    }
}
