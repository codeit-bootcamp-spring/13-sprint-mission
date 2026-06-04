package com.sprint.mission.discodeit.service.basic;


import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {
    BinaryContentRepository bcr;

    @Override
    public void create(BinaryContent bci){
        bcr.save(
                BinaryContent.builder()
                        .contentID(bci.getContentID())
                        .authorID(bci.getAuthorID())
                        .build()
        );
    }

    @Override
    public BinaryContent find(UUID id){
        return bcr.findByID(id);
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids){
        return ids.stream()
                .map(bcr::findByID)
                .toList();
    }

    @Override
    public void delete(UUID id){
        bcr.delete(id);
    }

}
