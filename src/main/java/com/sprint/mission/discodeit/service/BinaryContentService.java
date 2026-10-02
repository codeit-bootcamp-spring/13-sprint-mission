package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;

import java.util.List;
import java.util.UUID;

public interface BinaryContentService {

    BinaryContentDto createBinaryContent(BinaryContentCreateRequest request);
    BinaryContentDto getBinaryContent(UUID binaryContentId);
    List<BinaryContentDto> getBinaryContentsByIdIn(List<UUID> binaryContentIds);
    BinaryContentDto updateStatus(UUID binaryContentId, BinaryContentStatus binaryContentStatus);
    void deleteBinaryContent(UUID binaryContentId);

}
