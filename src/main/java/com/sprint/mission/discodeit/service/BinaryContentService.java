package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.UUID;

public interface BinaryContentService {
    BinaryContentDto create(BinaryContentCreateRequest request);
    BinaryContent createEntity(BinaryContentCreateRequest request);
    BinaryContentDto findById(UUID id);
    Collection<BinaryContentDto> findAllByIdIn(Collection<UUID> ids);
    void delete(UUID id);
    BinaryContent findEntityById(UUID id);
    ResponseEntity<?> download(UUID id);

}
