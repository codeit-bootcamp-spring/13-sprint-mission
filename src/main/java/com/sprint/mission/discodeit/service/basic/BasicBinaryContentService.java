package com.sprint.mission.discodeit.service.basic;


import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentStorage binaryContentStorage;
    private final MapStructMapper mapStructMapper;

    @Override
    @Transactional(readOnly = true)
    public BinaryContentDto findByID(UUID id){
        // todo - BinaryContentException 작성, ㅅㅓㄹ정.
        return binaryContentRepository.getBinaryContentById(id)
                .orElseThrow(RuntimeException::new);
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
                binaryContentRepository.findById(id).orElseThrow(RuntimeException::new)
        );
    }

}
