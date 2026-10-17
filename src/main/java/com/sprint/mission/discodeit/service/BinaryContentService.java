package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.command.CreateBinaryContentCommand;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;

import java.util.List;
import java.util.UUID;

public interface BinaryContentService {

    BinaryContentDto create(CreateBinaryContentCommand command);

    BinaryContentDto find(UUID id);

    List<BinaryContentDto> findAllByIdIn (List<UUID> ids);

    void delete(UUID id);

    BinaryContentDto updateStatus(
            UUID binaryContentId,
            BinaryContentStatus status
    );
}
