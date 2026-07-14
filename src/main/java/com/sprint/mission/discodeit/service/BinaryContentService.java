package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.UserStatusUpdateResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.UUID;

public interface BinaryContentService {

    BinaryContentDto createBinaryContent(BinaryContentCreateRequest request);
    BinaryContentDto findBinaryContentById(UUID binaryContentId);
    List<BinaryContentDto> findAllBinaryContentByIdIn(List<UUID> binaryContentIds);
    void deleteBinaryContent(UUID binaryContentId);

}
