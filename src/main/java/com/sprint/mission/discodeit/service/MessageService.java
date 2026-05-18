package com.sprint.mission.discodeit.service;


import com.sprint.mission.discodeit.entity.Message;


import java.util.ArrayList;
import java.util.UUID;

public interface MessageService {
    void createMessage(UUID user, UUID channel, String data);
    ArrayList<Message> readMessage(UUID id);
    ArrayList<Message> readMessageAll();
    void updateMessage(UUID id, String text);
    void deleteMessage(UUID id);
}
