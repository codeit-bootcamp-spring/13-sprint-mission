package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.*;

public class JCFMessageService implements MessageService {
    private final Map<UUID, Message> data;

    public JCFMessageService() {
        this.data = new HashMap<>();
    }

    @Override
    public Message create(UUID userId, UUID channelId, String content) {
        Message message = new Message(userId, channelId, content);
        data.put(message.getId(), message);
        System.out.println("메시지가 작성되었습니다!");
        return message;
    }

    @Override
    public Message read(UUID id) {
        if (!data.containsKey(id)) {
            System.out.println("메시지가 작성되지 않았습니다.");
        }
        return data.get(id);
    }

    @Override
    public List<Message> readAll() {
        return data.values().stream()
                .toList();
    }

    @Override
    public void update(UUID id, String content) {
        if(data.containsKey(id)){
            Message message = data.get(id);
            message.update(content);
        }
    }

    @Override
    public void delete(UUID id) {
        if(!data.containsKey(id)){
            System.out.println("메시지가 작성되지 않았습니다.");
        }
        data.remove(id);
    }
}
