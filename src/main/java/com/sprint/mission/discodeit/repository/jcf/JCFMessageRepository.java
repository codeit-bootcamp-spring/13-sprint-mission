package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.*;

public class JCFMessageRepository implements MessageRepository {

  private final Map<UUID, Message> database = new HashMap<>();


  @Override
  public Message save(Message message) {

    database.put(message.getId(), message);

    return message;
  }

  @Override
  public Optional<Message> findById(UUID id) {
    return Optional.ofNullable(database.get(id));
  }

  @Override
  public List<Message> findAll() {
    return new ArrayList<>(database.values());
  }

  @Override
  public void delete(UUID id) {
    database.remove(id);
  }

  @Override
  public void deleteByChannelId(UUID channelId) {
    database.values().removeIf(rs -> rs.getChannelId() != null
        && rs.getChannelId().equals(channelId));
  }
}
