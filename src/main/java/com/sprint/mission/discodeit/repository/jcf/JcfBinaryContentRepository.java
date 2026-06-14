package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JcfBinaryContentRepository implements BinaryContentRepository {

    private final List<BinaryContent> database = new ArrayList<>();

    @Override
    public void create(BinaryContent binaryContent) {
        database.add(binaryContent);
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        return database.stream()
                .filter(bc -> bc.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<BinaryContent> findAll() {
        return new ArrayList<>(database);
    }

    @Override
    public void delete(UUID id) {
        database.removeIf(bc -> bc.getId().equals(id));
    }
}
