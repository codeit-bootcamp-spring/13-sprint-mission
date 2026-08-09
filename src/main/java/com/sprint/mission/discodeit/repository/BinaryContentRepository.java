package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.*;
import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface BinaryContentRepository extends JpaRepository<BinaryContent, UUID> {

    List<BinaryContent> findAllByIdIn(List<UUID> ids);

}
