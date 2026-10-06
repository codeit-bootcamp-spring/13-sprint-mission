package com.sprint.mission.discodeit.service.basic;


import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentStorage binaryContentStorage;
    private final MapStructMapper mapStructMapper;

    private final ApplicationEventPublisher eventPublisher;

    // todo - 구현.
    @Transactional
    public void create(){

        binaryContentRepository.save(
                new BinaryContent(
                        "filename",
                        "contentType",
                        0L
                )
        );

        eventPublisher.publishEvent(new BinaryContentCreatedEvent());
    }

    @Override
    @Transactional(readOnly = true)
    public BinaryContentDto findByID(UUID id){
        return binaryContentRepository.getBinaryContentById(id)
                .orElseThrow(() -> new BinaryContentNotFoundException(id.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BinaryContentDto> findAllByIdIn(List<UUID> ids){
        return binaryContentRepository.getBinaryContentsInIdList(ids).values().stream().toList();
    }

    @Override
    @Transactional
    public void delete(UUID id){
        binaryContentRepository.deleteBinaryContent(
                binaryContentRepository.findById(id)
                        .orElseThrow(() -> new BinaryContentNotFoundException(id.toString()))
        );
    }

    @Override
    @Transactional(propagation = Propagation.NESTED)    // 동일 클래스 내부 선언이므로, 트랜젝션을 분리하기위해 항상 새 트랜젝션을 엶
    public BinaryContentDto updateStatus(UUID id, BinaryContentStatus status){
        // todo - DTO 반환 구현.
        // 요청에 데이터를 반환해야 하기 때문에, 데이터 읽는 건 하나의 요청 흐름이 끝나기 전에 완료 해야 함.
        // 데이터 읽는 어댑터를 서비스에 넣을까? -> 트랜젝션이 길어지니 문제가 생기진 않을까? -> 데이터 주입과 Dto 반환을 분리하면 좋을까?
        BinaryContent binaryContent = binaryContentRepository.findById(id)
                .orElseThrow(() -> new BinaryContentNotFoundException(id.toString()));

        binaryContent.setStatus(status);

        return mapStructMapper.toDto(binaryContent);
    }

}
