package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.JCFException;
import com.sprint.mission.discodeit.service.MessageService;


import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class FileMessageService extends FileBase implements MessageService {

    public FileMessageService(Path path) {
        super(path);
    }



    @Override
    public void createMessage(UUID user, UUID channel, String data){
        HashMap<UUID, Message> msgList = this.load();

        Message msg = new Message(user, channel, data);
        for (int i = 0; i < 3; i++){
            if (msgList.containsKey(msg.getId())) msg = new Message(user, channel, data);
        }
        msgList.put(msg.getId(),msg);
        this.save(msgList);
    }

    @Override
    public ArrayList<Message> readMessage(UUID id){
        HashMap<UUID, Message> msgList = this.load();
        if(!msgList.containsKey(id)) JCFException.throwRuntimeError("Message dos not exist.");
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(msgList.get(id));
        return messages;
    }

    @Override
    public ArrayList<Message> readMessageAll(){
        HashMap<UUID, Message> msgList = this.load();
        return new ArrayList<>(msgList.values());
    }

    @Override
    public void updateMessage(UUID id, String text){
        HashMap<UUID, Message> msgList = this.load();
        Message msg = msgList.get(id);
        msg.setUpdatedAt(System.currentTimeMillis());
        msg.setMessages(text);
        this.save(msgList);
    }

    @Override
    public void deleteMessage(UUID id){
        HashMap<UUID, Message> msgList = this.load();
        Message msg = msgList.remove(id);
        if ( msg == null) JCFException.throwRuntimeError("Message dos not exist.");
        this.save(msgList);
    }
}
