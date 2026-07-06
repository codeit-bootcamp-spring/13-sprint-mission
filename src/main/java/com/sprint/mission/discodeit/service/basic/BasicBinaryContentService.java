package com.sprint.mission.discodeit.service.basic;


import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.repository.JPABinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {
    private final JPABinaryContentRepository bcr;

    @Override
    public BinaryContent findByID(UUID id){
        return bcr.findById(id).stream().findFirst().orElseThrow(
                () -> new DiscodeitException("Content not existed ","BinaryContent",404)
        );
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids){
        return ids.stream()
                .map(id -> bcr.findById(id).stream().findFirst().orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional
    public void delete(UUID id){
        bcr.deleteById(id);
    }

}
