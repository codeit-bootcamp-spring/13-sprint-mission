package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.entity.*;
import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

   List<ReadStatus> findAllByChannelId(UUID channelId);

   List<ReadStatus> findAllByUserId(UUID userId);

   Optional<ReadStatus> findByChannelIdAndUserId(
           UUID channelId,
           UUID userId
   );

}