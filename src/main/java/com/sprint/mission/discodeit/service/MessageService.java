package com.sprint.mission.discodeit.service;


import com.sprint.mission.discodeit.entity.Message;


import java.util.ArrayList;
import java.util.UUID;

public interface MessageService {
    void createMessage(UUID user, UUID channel, String data);
    ArrayList<Message> getMessageById(UUID id);
    ArrayList<Message> getMessageList();
    void updateMessageData(UUID id, String data);
    void deleteMessage(UUID id);
}
