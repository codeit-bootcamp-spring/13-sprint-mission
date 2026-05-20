package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class JCFMessageRepository implements MessageRepository {
    private final HashMap<UUID, Message> data;

    public JCFMessageRepository() {
        data = new HashMap<>();
    }

    @Override
    public void create(User user, Channel channel, String data) {
        for (int i = 0; i < 3; i++){
            Message msg = new Message(user.getId(), channel.getId(), data);
            if (!this.data.containsKey(msg.getId())) {
                this.data.put(msg.getId(),msg);
                break;
            }
        }
    }

    @Override
    public ArrayList<Message> select (JCFSelectFilter fn) {
        return new ArrayList<>(data.values().stream()
                .filter(fn::filter)
                .toList());
    }

    @Override
    public void update(Message msg, String data) {
        msg.setUpdatedAt(System.currentTimeMillis());
        msg.setMessages(data);
    }

    @Override
    public void delete(Message message) {
        data.remove(message.getId());
    }
}
