package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    void deleteByChannelId(UUID channelId);

    List<Message> findAllByChannelId(UUID channelId);

    Slice<Message> findAllByChannelId(UUID channelId, Pageable pageable);

}