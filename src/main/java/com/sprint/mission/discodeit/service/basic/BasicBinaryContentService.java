package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
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
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentStorage binaryContentStorage;
    private final BinaryContentMapper binaryContentMapper;

    @Override
    public BinaryContentDto create(BinaryContentCreateRequest request) {
        BinaryContent content = BinaryContent.create(
                request.fileName(),
                request.size(),
                request.contentType()
        );
        BinaryContent savedContent = binaryContentRepository.save(content);
        binaryContentStorage.put(savedContent.getId(), request.bytes());

        log.info("BinaryContent: {}가 생성됨.", savedContent.getFileName());
        return binaryContentMapper.toDto(savedContent);
    }

    @Transactional(readOnly = true)
    @Override
    public BinaryContentDto find(UUID binaryContentId) {
        return binaryContentRepository.findById(binaryContentId)
                .map(binaryContentMapper::toDto)
                .orElseThrow(() -> new NoSuchElementException("바이너리 콘텐츠 ID: " + binaryContentId + " 를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    @Override
    public List<BinaryContentDto> findAllByIdIn(List<UUID> binaryContentIds) {
        return binaryContentRepository.findAllByIdIn(binaryContentIds).stream()
                .map(binaryContentMapper::toDto)
                .toList();
    }

    @Override
    public void delete(UUID binaryContentId) {
        if (!binaryContentRepository.existsById(binaryContentId)) {
            throw new NoSuchElementException("바이너리 콘텐츠 ID: " + binaryContentId + " 를 찾을 수 없습니다.");
        }
        binaryContentRepository.deleteById(binaryContentId);
        log.info("BinaryContent: {}가 삭제됨.", binaryContentId);
    }
}