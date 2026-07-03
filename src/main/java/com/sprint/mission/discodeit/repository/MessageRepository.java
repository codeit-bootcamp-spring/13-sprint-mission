package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

//    void createMessage(Message message);
//    Optional<Message> findMessageById(UUID id);
//    List<Message> findAllMessagesByChannelId(UUID channelId);
//    List<Message> findAllMessagesByUserId(UUID userId);
//    void save();
//    void deleteMessagesByChannelId(UUID channelId);
//    void deleteMessageById(UUID id);

    List<Message> findAllByChannelId(UUID channelId);
    List<Message> findAllByUserId(UUID userId);
    void deleteAllByChannelId(UUID channelId);

}
