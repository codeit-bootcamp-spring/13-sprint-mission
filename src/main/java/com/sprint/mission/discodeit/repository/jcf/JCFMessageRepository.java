package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Predicate;

public class JCFMessageRepository implements MessageRepository {
    private final HashMap<UUID, Message> data;

    private static class JMR {
        private static final JCFMessageRepository INSTANCE = new JCFMessageRepository();
    }

    private JCFMessageRepository() {
        data = new HashMap<>();
    }

    public static JCFMessageRepository open() {
        return JMR.INSTANCE;
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
    public ArrayList<Message> select (Predicate<Message> fn) {
        return new ArrayList<>(data.values().stream()
                .filter(fn)
                .toList());
    }

    @Override
    public void update(UUID id, String data) {
        Message msg = this.data.get(id);
        msg.setUpdatedAt(System.currentTimeMillis());
        msg.setMessages(data);
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }


    // File * Repository 와의 호환성을 위한 더미 메서드
    public void close(){}
}
