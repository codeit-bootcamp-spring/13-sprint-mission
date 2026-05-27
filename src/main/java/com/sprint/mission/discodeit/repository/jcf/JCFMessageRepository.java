package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFMessageRepository implements MessageRepository {
    private final List<Message> data = new ArrayList<>();
    @Override
    public void save(Message message) {
        data.add(message);
    }

    @Override
    public Message findById(UUID id) {
        for(Message message : data){
            if(message.getId().equals(id)){
                return message;
            }
        }
        return null;
    }

    @Override
    public List<Message> findAll() {
        return data;
    }

    @Override
    public void delete(UUID id) {
        data.remove(findById(id));

    }
}
