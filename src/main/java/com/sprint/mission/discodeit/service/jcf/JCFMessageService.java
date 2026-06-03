package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.*;

public class JCFMessageService extends MessageService {
    private final Map<UUID, Message> data;

    public JCFMessageService(){
        this.data = new HashMap<>();
    }

    @Override
    public void create(Message message){
        data.put(message.getId(), message);
    }

    @Override
    public Message read(UUID id){
        return data.get(id);
    }

    @Override
    public List<Message> readAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void update(Message message){
        data.put(message.getId(), message);
    }

    @Override
    public void delete(UUID id){
        data.remove(id);
    }
}
