package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentMapper binaryContentMapper;

    @Override
    public BinaryContentDto find(UUID id) {
        BinaryContent binaryContent = binaryContentRepository.findById(id)
                .orElseThrow(()->new BinaryContentNotFoundException(id));

        return binaryContentMapper.toDto(binaryContent);
    }

    @Override
    public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
        List<BinaryContentDto> responses = new ArrayList<>();
        List<BinaryContent> binaryContents = binaryContentRepository.findAllById(ids);

        for (BinaryContent binaryContent : binaryContents) {
            responses.add(binaryContentMapper.toDto(binaryContent));
        }
        return responses;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!binaryContentRepository.existsById(id)) {
            throw new BinaryContentNotFoundException(id);
        }
        binaryContentRepository.deleteById(id);
    }
}
