package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.command.binarycontent.BinaryContentCreateCommand;
import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.UUID;

public interface BinaryContentService {

    BinaryContentDto create(BinaryContentCreateCommand command);
    BinaryContentDto find(UUID id);
    List<BinaryContentDto> findAllByIdIn(List<UUID> binaryContentIds);
    void delete(UUID id);
    BinaryContent findEntity(UUID id);
}
