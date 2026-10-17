package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.CreateBinaryContentCommand;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository repository;
    private final BinaryContentMapper binaryContentMapper;
    private final BinaryContentStorage binaryContentStorage;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public BinaryContentDto create(CreateBinaryContentCommand command) {
        if (command == null)  {
            throw new IllegalArgumentException("바이너리 콘텐츠 생성 요청은 필수입니다.");
        }

        log.info(
                "파일 업로드 요청. fileName={}, contentType={}, size={}",
                command.fileName(),
                command.contentType(),
                command.bytes().length
        );

        BinaryContent binaryContent = new BinaryContent(
                command.fileName(),
                (long)command.bytes().length,
                command.contentType()
        );

        repository.save(binaryContent);

        eventPublisher.publishEvent(new BinaryContentCreatedEvent(binaryContent.getId(), command.bytes()));

        log.info(
                "바이너리 콘텐츠 메타데이터 저장 및 이벤트 발행 완료. id={}, fileName={}",
                binaryContent.getId(),
                binaryContent.getFileName()
        );

        return binaryContentMapper.toDto(binaryContent);
    }

    @Override
    public BinaryContentDto find(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "바이너리 콘텐츠 ID는 필수입니다."
            );
        }

        BinaryContent binaryContent = repository.findById(id)
                .orElseThrow(() ->
                        new BinaryContentNotFoundException(id)
                );

        return binaryContentMapper.toDto(binaryContent);
    }

    @Override
    public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("바이너리 콘텐츠 ID 목록은 필수입니다.");
        }

        List<BinaryContent> binaryContents = repository.findAllByIdIn(ids);
        return binaryContentMapper.toDtoList(binaryContents);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("바이너리 콘텐츠 ID는 필수입니다.");
        }

        BinaryContent binaryContent = repository.findById(id)
                .orElseThrow(() -> new BinaryContentNotFoundException(id));

        repository.delete(binaryContent);
        log.info("파일 삭제 완료. id={}", id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BinaryContentDto updateStatus(UUID binaryContentId, BinaryContentStatus status) {
        if (binaryContentId == null) {
            throw new IllegalArgumentException("바이너리 콘텐츠 ID는 필수입니다.");
        }

        if (status == null) {
            throw new IllegalArgumentException("바이너리 콘텐츠 상태는 필수입니다.");
        }

        BinaryContent  binaryContent = repository.findById(binaryContentId)
                .orElseThrow(() -> new BinaryContentNotFoundException(binaryContentId));

        binaryContent.updateStatus(status);
        return binaryContentMapper.toDto(binaryContent);
    }

}
