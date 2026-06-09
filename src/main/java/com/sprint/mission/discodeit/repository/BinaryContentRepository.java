package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface BinaryContentRepository {
    List<BinaryContent> findAllByMessageId(UUID id);
}
