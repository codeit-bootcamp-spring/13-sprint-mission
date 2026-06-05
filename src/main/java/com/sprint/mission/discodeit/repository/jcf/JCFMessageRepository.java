package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@NoArgsConstructor
public class JCFMessageRepository implements MessageRepository {

    //필드
    private final List<Message> messages = new ArrayList<>();

    //interface
    @Override
    public void createMessage(Message message) {
        messages.add(message);
    }

    @Override
    public Optional<Message> findMessageById(UUID id) {
        return messages.stream()
                .filter(message -> message.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Message> findAllMessagesByChannelId(UUID channelId) {
        return messages.stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .toList();
    }

    @Override
    public void save() {

    }

    @Override
    public void deleteMessagesByChannelId(UUID channelId) {
        messages.removeAll(
                messages.stream()
                        .filter(message -> message.getChannelId().equals(channelId))
                        .toList()
        );
    }

    @Override
    public void deleteMessageById(UUID id) {
        messages.remove(
                messages.stream()
                        .filter(message -> message.getId().equals(id))
                        .findFirst()
                        .get()
        );
    }
}
