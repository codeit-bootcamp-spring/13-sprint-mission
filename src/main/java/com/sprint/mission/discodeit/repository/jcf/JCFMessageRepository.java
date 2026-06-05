package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//메시지를 메모리(ArrayList)에 저장하는 Repository
public class JCFMessageRepository implements MessageRepository {
    //메시지 저장 리스트
    private final List<Message> messages = new ArrayList<>();

    //메시지 저장
    public Message save(Message message) {
        messages.add(message);
        return message;
    }

    //id로 메시지 조회
    public Message findById(UUID id) {
        for (Message m : messages) {
            if (m.getId().equals(id))
                return m;
        }
        return null;
    }

    @Override
    public List<Message> findAll() {
        return new ArrayList<>(messages);
    }

    @Override
    public void delete(UUID id) {

        messages.removeIf(
                message -> message.getId().equals(id)
        );
    }
}
