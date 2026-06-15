package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@NoArgsConstructor
@ConditionalOnProperty(
        value = "discodeit.repository.type",
        havingValue = "jcf",
        matchIfMissing = true
)
public class JCFBinaryContentRepository implements BinaryContentRepository {

    //필드
    private final List<BinaryContent> binaryContents = new ArrayList<>();

    //interface
    @Override
    public void createBinaryContent(BinaryContent binaryContent) {
        binaryContents.add(binaryContent);
    }

    @Override
    public Optional<BinaryContent> findBinaryContentById(UUID binaryContentId) {
        return binaryContents.stream()
                .filter(binaryContent -> binaryContent.getId().equals(binaryContentId))
                .findFirst();
    }

    @Override
    public List<BinaryContent> findAllBinaryContentByIdIn(List<UUID> binaryContentIds) {
        return binaryContents.stream()
                .filter(binaryContent -> binaryContentIds.contains(binaryContent.getId()))
                .toList();
    }

    @Override
    public void deleteBinaryContent(UUID id) {
        binaryContents.remove(
                binaryContents.stream()
                        .filter(binaryContent -> binaryContent.getId().equals(id))
                        .findFirst()
                        .get()
        );
    }
}
