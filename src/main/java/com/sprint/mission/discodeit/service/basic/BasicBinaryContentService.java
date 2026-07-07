package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentMapper binaryContentMapper;
    private final BinaryContentStorage binaryContentStorage;


    @Override
    public BinaryContentDto create(BinaryContentCreateRequest request) {
        BinaryContent binaryContent = new BinaryContent(
                request.fileName(),
                request.fileSize(),
                request.contentType()
        );
        binaryContentRepository.save(binaryContent);
        binaryContentStorage.put(binaryContent.getId(), request.bytes());
        return binaryContentMapper.toDto(binaryContent);
    }

    @Override
    @Transactional(readOnly = true)
    public BinaryContentDto find(UUID id) {
        BinaryContent findByContent = binaryContentRepository
                .findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 content 입니다."));
        return binaryContentMapper.toDto(findByContent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
        return ids.stream()
                .map(id ->
                        binaryContentMapper.toDto(binaryContentRepository.findById(id)
                                .orElseThrow(()-> new NoSuchElementException("존재하지 않는 content 입니다."))))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        binaryContentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 content 입니다."));
        binaryContentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BinaryContent findEntity(UUID id) {
        return binaryContentRepository.findById(id)
                .orElseThrow(()-> new NoSuchElementException("존재하지 않는 content 입니다."));
    }
}

