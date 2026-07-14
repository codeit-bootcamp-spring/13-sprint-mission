package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

   List<ReadStatus> findAllByChannelId(UUID channelId);

   List<ReadStatus> findAllByUserId(UUID userId);

   Optional<ReadStatus> findByChannelIdAndUserId(UUID userId, UUID channelId);

}