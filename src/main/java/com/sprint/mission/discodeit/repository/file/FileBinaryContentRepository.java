package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FileBinaryContentRepository extends FileRepositoryRoot<BinaryContent> implements BinaryContentRepository {

    //ctor
    public FileBinaryContentRepository() {
        super(Path.of("data/binaryContents.ser"));
    }

    //interface
    @Override
    public void createBinaryContent(BinaryContent binaryContent) {
        storage.add(binaryContent);

        saveToBinary();
    }

    @Override
    public Optional<BinaryContent> findBinaryContentByContentPath(String contentPath) {
        return storage.stream()
                .filter(binaryContent -> binaryContent.getContentPath().equals(contentPath))
                .findFirst();
    }

    @Override
    public Optional<BinaryContent> findBinaryContentById(UUID binaryContentId) {
        return storage.stream()
                .filter(binaryContent -> binaryContent.getId().equals(binaryContentId))
                .findFirst();
    }

    @Override
    public List<BinaryContent> findAllBinaryContentByIdIn(List<UUID> binaryContentIds) {
        return storage.stream()
                .filter(binaryContent -> binaryContentIds.contains(binaryContent.getId()))
                .toList();
    }

    @Override
    public void deleteBinaryContent(UUID id) {
        storage.remove(
                storage.stream()
                        .filter(binaryContent -> binaryContent.getId().equals(id))
                        .findFirst()
                        .get()
        );

        saveToBinary();
    }
}
