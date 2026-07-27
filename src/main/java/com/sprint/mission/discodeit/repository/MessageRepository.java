package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.time.*;
import java.util.*;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByChannelId(UUID channelId);

    Slice<Message> findByChannelId(UUID channelId, Pageable pageable);

    Slice<Message> findByChannelIdAndCreatedAtLessThan(
            UUID channelId,
            Instant cursor,
            Pageable pageable
    );

}
