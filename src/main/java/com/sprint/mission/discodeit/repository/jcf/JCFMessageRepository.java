package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import lombok.NoArgsConstructor;

import java.util.*;

@NoArgsConstructor
public class JCFMessageRepository implements MessageRepository {

    private static final Map<UUID, Message> data = new HashMap<>(); // 인스턴스를 생성할 때 마다 새로운 Map을 만들지 않도록 한다

    @Override
    public Message save(Message message) {
        data.put(message.getId(), message);
        return message;
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<Message> findAll() {
        return data.values().stream().toList();
    }

    @Override
    public void deleteById(UUID id) {
        data.remove(id);
    }

    @Override
    public boolean existById(UUID id) {
        return data.containsKey(id);
    }

    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        return data.values().stream().toList();
    }
}
