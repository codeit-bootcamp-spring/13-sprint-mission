package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

//    void createMessage(Message message);
//    Optional<Message> findMessageById(UUID id);
//    List<Message> findAllMessagesByChannelId(UUID channelId);
//    List<Message> findAllMessagesByUserId(UUID userId);
//    void save();
//    void deleteMessagesByChannelId(UUID channelId);
//    void deleteMessageById(UUID id);

    Slice<Message> findAllByChannelId(UUID channelId, Pageable pageable);
    List<Message> findAllByChannelId(UUID channelId);
    List<Message> findAllByAuthorId(UUID authorId);
    void deleteAllByChannelId(UUID channelId);

}
