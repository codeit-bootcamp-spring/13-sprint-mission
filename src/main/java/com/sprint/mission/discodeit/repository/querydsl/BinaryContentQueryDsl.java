package com.sprint.mission.discodeit.repository.querydsl;

import com.sprint.mission.discodeit.dto.request.MultipartFileDto;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


public interface BinaryContentQueryDsl {

    BinaryContent saveWithMultipartCommand(MultipartFileDto command);
    List<BinaryContent> saveAllFromMultipartFileDtoList(List<MultipartFileDto> files);
    Optional<BinaryContentDto> getBinaryContentById(UUID id);
    Map<UUID,BinaryContentDto> getBinaryContentsInIdList(List<UUID> list);
    void deleteBinaryContent(BinaryContent target);
}
