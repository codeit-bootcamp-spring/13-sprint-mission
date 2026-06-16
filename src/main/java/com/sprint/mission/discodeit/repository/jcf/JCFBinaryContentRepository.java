package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;

import java.util.UUID;

public class JCFBinaryContentRepository implements BinaryContentRepository {

    @Override
    public BinaryContent findByUserId(UUID userId) {
        return null;
    }

    @Override
    public void delete(UUID id) {

    }
}
