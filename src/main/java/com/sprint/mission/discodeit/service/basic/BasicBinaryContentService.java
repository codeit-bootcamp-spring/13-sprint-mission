package com.sprint.mission.discodeit.service.basic;


import com.sprint.mission.discodeit.dto.input.BinaryContentInput;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
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

    @Override
    public BinaryContent create(BinaryContentInput bci){
        BinaryContent bc = BinaryContent.builder()
                .contentID(bci.getContentID())
                .authorID(bci.getAuthorID())
                .build();
        bcr.save(bc);
        return bc;
    }

    @Override
    public BinaryContent find(UUID id){
        return bcr.findByID(id);
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids){
        return ids.stream()
                .map(bcr::findByID)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void delete(UUID id){
        bcr.delete(id);
    }

}
