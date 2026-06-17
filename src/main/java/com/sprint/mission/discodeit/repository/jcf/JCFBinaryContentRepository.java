package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.stereotype.*;

import java.util.*;
@Repository
@ConditionalOnProperty(name = "discodeit.repository.type",  matchIfMissing = true, havingValue = "jcf")
public class JCFBinaryContentRepository implements BinaryContentRepository {

    private final Map<UUID, BinaryContent> data;

    public JCFBinaryContentRepository() {
        this.data = new HashMap<>();
    }

    @Override
    public BinaryContent findByUserId(UUID id) {
        return data.values().stream()
                .filter(content -> content.getUserId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<BinaryContent> findAllByMessageId(UUID id) {
        return data.values().stream()
                .filter(content -> content.getMessageId().equals(id))
                .toList();
    }

    @Override
    public void create(BinaryContent binaryContent) {
        data.put(binaryContent.getId(), binaryContent);
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        return data.values().stream()
                .filter(content -> ids.contains(content.getId()))
                .toList();
    }

    @Override
    public BinaryContent find(UUID id) {
        return data.get(id);
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}
