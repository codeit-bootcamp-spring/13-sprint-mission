package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;

    @Override
    public BinaryContent create(BinaryContentCreateRequest request) {
        BinaryContent binaryContent = new BinaryContent(
                request.userId(),
                request.fileName(),
                request.fileSize(),
                request.contentType(),
                request.bytes()
        );
        binaryContentRepository.save(binaryContent);
        return binaryContent;
    }

    @Override
    public BinaryContent find(UUID id) {
        BinaryContent findByContent = binaryContentRepository
                .find(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 content 입니다."));
        return  findByContent;
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        return ids.stream()
                .map(id -> binaryContentRepository.find(id).
                        orElseThrow(() -> new NoSuchElementException("존재하지 않는 content 입니다.")))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        binaryContentRepository.find(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 content 입니다."));
        binaryContentRepository.delete(id);
    }
}

