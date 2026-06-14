package com.sprint.mission.discodeit.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BinaryContentRepository {
    BinaryContentRepository save(BinaryContentRepository binaryContentRepository);
    Optional<BinaryContentRepository> findById(UUID id);
    List<BinaryContentRepository> findAll();
    boolean existsById(UUID id);
    void deleteById(UUID id);
}
