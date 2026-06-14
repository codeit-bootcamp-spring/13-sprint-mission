package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;

    @Override
    public BinaryContentResponse create(BinaryContentRequest dto) {
        // 💡 리아님이 작성하신 BinaryContent 엔티티 생성자 양식에 맞추어 인자를 밀어 넣습니다.
        BinaryContent binaryContent = new BinaryContent(dto.userId(), dto.messageId(), dto.fileName());
        binaryContentRepository.create(binaryContent);

        return new BinaryContentResponse(
                binaryContent.getId(),
                binaryContent.getCreatedAt(),
                binaryContent.getUserId(),
                binaryContent.getMessageId(),
                binaryContent.getFileName()
        );
    }

    @Override
    public Optional<BinaryContentResponse> find(UUID id) {
        // [요구사항] id로 조회합니다.
        return binaryContentRepository.findById(id) // 💡 레포지토리 단건 조회 명칭에 맞추세요.
                .map(bc -> new BinaryContentResponse(
                        bc.getId(),
                        bc.getCreatedAt(),
                        bc.getUserId(),
                        bc.getMessageId(),
                        bc.getFileName()
                ));
    }

    @Override
    public List<BinaryContentResponse> findAllByIdIn(List<UUID> ids) {
        // [요구사항] id 목록을 필터 조건으로 포함하여 전체 창고에서 매칭되는 목록을 정석대로 끄집어냅니다.
        return binaryContentRepository.findAll().stream()
                .filter(bc -> ids.contains(bc.getId()))
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
        // [요구사항] id로 삭제합니다.
        binaryContentRepository.delete(id);
    }
}
