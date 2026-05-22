package com.sprint.mission.discodeit.service.jcf;


import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class JCFMessageService implements MessageService {

    private final HashMap<UUID, Message> data;

    private static class UserInstance{
        private static final JCFMessageService INSTANCE = new JCFMessageService();
    }

    private JCFMessageService(){
        data = new HashMap<>();
    }
    public static JCFMessageService getInstance(){
        return UserInstance.INSTANCE;
    }


    @Override
    public void createMessage(UUID user, UUID channel, String data){
        Message msg = new Message(user, channel, data);
        for (int i = 0; i < 3; i++){
            if (this.data.containsKey(msg.getId())) msg = new Message(user, channel, data);
        }
        this.data.put(msg.getId(),msg);
    }

    @Override
    public ArrayList<Message> readMessage(UUID id){
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(this.data.get(id));
        return messages;
    }

    @Override
    public ArrayList<Message> readMessageAll(){
        return new ArrayList<>(data.values());
    }

    @Override
    public void updateMessage(UUID id, String text){
        Message msg = data.get(id);
        msg.setUpdatedAt(System.currentTimeMillis());
        msg.setMessages(text);
    }

    @Override
    public void deleteMessage(UUID id){
        data.remove(id);
    }
}
