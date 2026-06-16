package com.sprint.mission.discodeit.service.basic;


import com.sprint.mission.discodeit.dto.input.BinaryContentInput;
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
    public BinaryContent create(BinaryContentInput bci){
        // check user exist.s
        ur.findByID(bci.auth()).orElseThrow(
                () -> new DiscodeitException("invalid Owner Id","BinaryContent",400)
        );

        BinaryContent bc = BinaryContent.builder()
                .contentID(bci.content())
                .authorID(bci.auth())
                .build();
        bcr.save(bc);
        return bc;
    }

    @Override
    public BinaryContent findByID(UUID id){
        return bcr.findByID(id).orElseThrow(
                () -> new DiscodeitException("Content not existed ","BinaryContent",400)
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
