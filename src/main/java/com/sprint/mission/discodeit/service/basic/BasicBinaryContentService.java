package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentMapper binaryContentMapper;
    private final BinaryContentStorage binaryContentStorage;

    @Override
    public BinaryContentResponse create(BinaryContentRequest dto) {

        log.debug("파일 업로드 시작: fileName={}, size={}, contentType={}",
                dto.fileName(), dto.size(), dto.contentType());

        BinaryContent binaryContent = new BinaryContent(dto.fileName(), dto.size(), dto.contentType());
        BinaryContent savedBinaryContent = binaryContentRepository.save(binaryContent);
        binaryContentStorage.put(savedBinaryContent.getId(), dto.bytes());

        log.info("파일 업로드 완료: binaryContentId={}, size={}",
                savedBinaryContent.getId(), savedBinaryContent.getSize());
        return binaryContentMapper.toDto(savedBinaryContent);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BinaryContentResponse> find(UUID id) {

        return binaryContentRepository.findById(id)
                .map(binaryContentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BinaryContentResponse> findAllByIdIn(List<UUID> ids) {

        return binaryContentRepository.findAllById(ids).stream()
                .map(binaryContentMapper::toDto)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        log.debug("파일 메타데이터 삭제 시작: binaryContentId={}", id);
        binaryContentRepository.deleteById(id);
        log.info("파일 메타데이터 삭제 완료: binaryContentId={}", id);
    }
}
