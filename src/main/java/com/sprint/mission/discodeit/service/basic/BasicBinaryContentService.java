package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {
    private final BinaryContentRepository binaryContentRepository;
    private final MapStructMapper mapStructMapper;


    @Override
    @Transactional(readOnly = true)
    public BinaryContentDto findByID(UUID id){
        return mapStructMapper.toDto(getBinaryContent(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BinaryContentDto> findAllByIdIn(List<UUID> ids){
        return binaryContentRepository.findAllById(ids)
                .stream()
                .map(mapStructMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(propagation = Propagation.NESTED)
    public BinaryContentDto updateStatus(UUID id, BinaryContentStatus status){
        BinaryContent binaryContent = getBinaryContent(id);

        binaryContent.setStatus(status);

        return mapStructMapper.toDto(binaryContent);
    }

    private BinaryContent getBinaryContent(UUID id){
        return binaryContentRepository.findById(id)
                .orElseThrow(() -> new BinaryContentNotFoundException(id.toString()));
    }

}
