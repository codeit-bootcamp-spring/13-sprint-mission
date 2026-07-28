package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentMapper binaryContentMapper;
    private final BinaryContentStorage binaryContentStorage;

    @Override
    @Transactional
    public BinaryContentDto create(BinaryContentCreateRequest request) {
        BinaryContent binaryContent = createEntity(request);
        return binaryContentMapper.toDto(binaryContent);
    }

    @Override
    @Transactional
    public BinaryContent createEntity(BinaryContentCreateRequest request) {
        log.info("Creating binary content: fileName={}, contentType={}, size={}",
                request.fileName(), request.contentType(), request.bytes().length);
        BinaryContent binaryContent = new BinaryContent(
                request.fileName(),
                request.contentType(),
                (long) request.bytes().length
        );

        binaryContentRepository.save(binaryContent);
        binaryContentStorage.put(binaryContent.getId(), request.bytes());

        return binaryContent;
    }

    @Override
    public BinaryContentDto findById(UUID id) {
        BinaryContent binaryContent = binaryContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다."));

        return binaryContentMapper.toDto(binaryContent);
    }

    @Override
    public Collection<BinaryContentDto> findAllByIdIn(Collection<UUID> ids) {
        return binaryContentRepository.findAllByIdIn(ids).stream()
                .map(binaryContentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting binary content: binaryContentId={}", id);
        if (!binaryContentRepository.existsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 파일입니다.");
        }

        binaryContentStorage.delete(id);
        binaryContentRepository.deleteById(id);
    }

    @Override
    public BinaryContent findEntityById(UUID id) {
        return binaryContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다."));
    }

    @Override
    public ResponseEntity<?> download(UUID id) {
        log.info("Downloading binary content: binaryContentId={}", id);
        BinaryContentDto binaryContentDto = findById(id);

        return binaryContentStorage.download(binaryContentDto);
    };

}
