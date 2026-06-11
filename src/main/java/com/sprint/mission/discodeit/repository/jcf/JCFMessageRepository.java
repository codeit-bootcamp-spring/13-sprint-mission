package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.*;

//메시지를 메모리(ArrayList)에 저장하는 Repository
public class JCFMessageRepository implements MessageRepository {
    private final Map<UUID, Message> data;
    public JCFMessageRepository() {this.data = new HashMap<>();}

    @Override //메시지 저장
    public Message save(Message message) {
        this.data.put(message.getId(), message);
        return message;
    }

    @Override //id로 메시지 조회
    public Optional<Message> findById(UUID id) {return Optional.ofNullable(this.data.get(id));}

    @Override
    public List<Message> findAll() {return this.data.values().stream().toList();}

    @Override
    public boolean existsById(UUID id) {return this.data.containsKey(id);}

    @Override
    public void deleteById(UUID id) {this.data.remove(id);}
}
