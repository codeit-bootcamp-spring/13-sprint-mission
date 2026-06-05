package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class FileBinaryContentRepository implements BinaryContentRepository {
    @Override
    public BinaryContent save(BinaryContent content) {
        return null;
    }

    @Override
    public BinaryContent findById(UUID id) {
        return null;
    }

    @Override
    public void delete(UUID id) {

    }
}
