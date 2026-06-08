package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

//@Repository
public class JCFBinaryContentRepository implements BinaryContentRepository {

    private final Map<UUID, BinaryContent> database = new HashMap<>();

    @Override
    public BinaryContent save(BinaryContent binaryContent) {
        database.put(binaryContent.getId(), binaryContent);
        return binaryContent;
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<BinaryContent> findAll() {
        return new ArrayList<>(database.values());
    }

    @Override
    public void delete(UUID id) {
        database.remove(id);
    }
}
