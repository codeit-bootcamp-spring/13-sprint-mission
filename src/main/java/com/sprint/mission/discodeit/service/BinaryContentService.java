package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusUpdateResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.UUID;

public interface BinaryContentService {

    BinaryContent createBinaryContent(BinaryContentCreateRequest request);
    BinaryContent findBinaryContentById(UUID binaryContentId);
    List<BinaryContent> findAllBinaryContentByIdIn(List<UUID> binaryContentIds);
    void deleteBinaryContent(UUID binaryContentId);

}
