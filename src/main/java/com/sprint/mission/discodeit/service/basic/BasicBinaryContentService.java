package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;

    @Override
    public BinaryContentResponse create(BinaryContentRequest dto) {

        BinaryContent binaryContent = new BinaryContent(dto.userId(), dto.messageId(), dto.fileName());
        binaryContentRepository.save(binaryContent);

        return new BinaryContentResponse(
                binaryContent.getId(),
                binaryContent.getCreatedAt(),
                binaryContent.getUserId(),
                binaryContent.getMessageId(),
                binaryContent.getFileName()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BinaryContentResponse> find(UUID id) {

        return binaryContentRepository.findById(id)
                .map(bc -> new BinaryContentResponse(
                        bc.getId(),
                        bc.getCreatedAt(),
                        bc.getUserId(),
                        bc.getMessageId(),
                        bc.getFileName()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BinaryContentResponse> findAllByIdIn(List<UUID> ids) {

        return binaryContentRepository.findAllById(ids).stream()
                .map(bc -> new BinaryContentResponse(
                        bc.getId(),
                        bc.getCreatedAt(),
                        bc.getUserId(),
                        bc.getMessageId(),
                        bc.getFileName()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        binaryContentRepository.deleteById(id);
    }
}
