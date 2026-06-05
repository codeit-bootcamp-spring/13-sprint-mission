package com.sprint.mission.discodeit.repository.jcf;


import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.HashMap;
import java.util.List;
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

    public static JCFMessageRepository getInstance() {
        return JMR.INSTANCE;
    }

    @Override
    public void save(Message msg) {
        this.data.put(msg.getId(),msg);
    }

    @Override
    public List<Message> find (Predicate<Message> fn) {
        return data.values().stream()
                .filter(fn)
                .toList();
    }

    @Override
    public List<Message> findByChannelID(UUID channelID) {
        return find(m -> m.getChannelID().equals(channelID));
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }

}
