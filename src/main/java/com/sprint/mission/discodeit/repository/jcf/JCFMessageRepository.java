package com.sprint.mission.discodeit.repository.jcf;


import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;


@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name="discodeit.repository.type",havingValue = "jcf", matchIfMissing = true)
public class JCFMessageRepository implements MessageRepository {
    private final HashMap<UUID, Message> data =  new HashMap<>();

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
