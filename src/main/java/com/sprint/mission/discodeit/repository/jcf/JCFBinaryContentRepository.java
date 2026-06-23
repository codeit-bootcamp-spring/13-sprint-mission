package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JCFBinaryContentRepository implements BinaryContentRepository {

    private final List<BinaryContent> data = new ArrayList<>();

    @Override
    public void save(BinaryContent content) {
        data.removeIf(c -> c.getId().equals(content.getId()));
        data.add(content);
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        return data.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<BinaryContent> findByUserId(UUID userId) {
        return data.stream()
                .filter(c -> c.getUserId().equals(userId))
                .toList();
    }

    @Override
    public List<BinaryContent> findAll() {
        return data;
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        return data.stream()
                .filter(c -> ids.contains(c.getId()))
                .toList();
    }

    @Override
    public List<BinaryContent> findAllByMessageId(UUID messageId) {
        return data.stream()
                .filter(c -> c.getMessageId().equals(messageId))
                .toList();
    }

    @Override
    public void delete(UUID id) {
        data.removeIf(c -> c.getId().equals(id));
    }
}
