package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class JCFMessageRepository implements MessageRepository {

    private final List<Message> data;

    public JCFMessageRepository(){
        this.data = new ArrayList<>();
    }

    @Override
    public void save(Message message) {
        data.add(message);
    }

    @Override
    public Message findById(UUID id) {
        for (Message m : data) {
            if (m.getId().equals(id)) {
                return m;
            }
        }
        throw new IllegalArgumentException("메시지를 찾을 수 없습니다.");
    }

    @Override
    public List<Message> findAll() {
        if (!data.isEmpty()) {
            return data;
        }
        return Collections.emptyList();
    }

    @Override
    public void delete(UUID id) {
        for (Message message : data) {
            if (message.getId().equals(id)) {
                data.remove(message);
                return;
            }
        }
        throw new IllegalArgumentException("메시지를 찾을 수 없습니다.");
    }
}
