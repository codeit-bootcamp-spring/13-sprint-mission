package com.sprint.mission.discodeit.service.jcf;


import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.JCFException;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class JCFMessageService implements MessageService {
    private final HashMap<UUID, Message> data;

    public JCFMessageService(){
        data = new HashMap<>();
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
        if(!this.data.containsKey(id)) JCFException.throwRuntimeError("Message dos not exist.");
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(this.data.get(id));
        return messages;
    }

    @Override
    public ArrayList<Message> readMessageAll(){
        ArrayList<Message> res = new ArrayList<>(data.values());
        return res;
    }

    @Override
    public void updateMessage(UUID id, String text){
        Message msg = data.get(id);
        msg.setUpdatedAt(System.currentTimeMillis());
        msg.setMessages(text);
    }

    @Override
    public void deleteMessage(UUID id){
        Message msg = data.remove(id);
        if ( msg == null) JCFException.throwRuntimeError("Message dos not exist.");
    }
}
