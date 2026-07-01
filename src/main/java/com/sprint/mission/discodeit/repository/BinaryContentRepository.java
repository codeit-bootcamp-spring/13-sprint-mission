package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.*;
import org.springframework.stereotype.*;

import java.util.*;

public interface BinaryContentRepository {
    List<BinaryContent> findAllByMessageId(UUID id);

    BinaryContent findByUserId(UUID id);

    void create(BinaryContent binaryContent);

    List<BinaryContent> findAllByIdIn(List<UUID> ids);

    BinaryContent find(UUID id);

    void delete(UUID id);

    boolean exists(UUID binaryContentId);
}
