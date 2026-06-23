package com.sprint.mission.discodeit.service.basic;


import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {
    private final BinaryContentRepository bcr;
    private final UserRepository ur;

    @Override
    public BinaryContent findByID(UUID id){
        return bcr.findByID(id).orElseThrow(
                () -> new DiscodeitException("Content not existed ","BinaryContent",404)
        );
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids){
        return ids.stream()
                .map(id -> bcr.findByID(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void delete(UUID id){
        bcr.delete(id);
    }

}
