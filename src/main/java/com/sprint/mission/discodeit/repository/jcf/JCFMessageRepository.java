package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.*;

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
    public Optional<Message> findById(UUID id) {
        for (Message m : data) {
            if (m.getId().equals(id)) {
                return Optional.of(m);
            }
        }

        return Optional.empty();
    }

    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        return data.stream()
                .filter(message ->
                        message.getChannelId().equals(channelId))
                .toList();
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
