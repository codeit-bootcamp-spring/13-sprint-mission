package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.FileException;
import com.sprint.mission.discodeit.exception.ResourceNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicBinaryContentService implements BinaryContentService {

    //필드
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentMapper binaryContentMapper;
    private final BinaryContentStorage binaryContentStorage;

    //interface
    @Override
    @Transactional
    public BinaryContentDto createBinaryContent(BinaryContentCreateRequest request) {
        BinaryContent binaryContent;
        try {
            //binaryContent 생성
            binaryContent = new BinaryContent(
                    request.file().getOriginalFilename(),
                    (long) request.file().getBytes().length,
                    request.file().getContentType()
            );
            binaryContent = binaryContentRepository.save(binaryContent);
            binaryContentStorage.put(binaryContent.getId(), request.file().getBytes());

            log.info("BinaryContent가 생성됨.");

        } catch (IOException e) {
            throw new FileException(e.getMessage());
        }

        return binaryContentMapper.toDto(binaryContent);
    }

    @Override
    @Transactional(readOnly = true)
    public BinaryContentDto getBinaryContent(UUID binaryContentId) {
        //BinaryContent 검색
        BinaryContent binaryContentTemp = binaryContentRepository.findById(binaryContentId)
                .orElseThrow(() -> new ResourceNotFoundException("에러: 해당 BinaryContent는 데이터파일에 존재하지 않습니다."));

        return binaryContentMapper.toDto(binaryContentTemp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BinaryContentDto> getBinaryContentsByIdIn(List<UUID> binaryContentIds) {
        //BinaryContent들 검색
        List<BinaryContent> binaryContentList = binaryContentRepository.findAllByIdIn(binaryContentIds);

        return binaryContentList.stream()
                .map(binaryContentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteBinaryContent(UUID binaryContentId) {
        //BinaryContent 검색
        BinaryContent binaryContentTemp = binaryContentRepository.findById(binaryContentId)
                .orElseThrow(() -> new ResourceNotFoundException("에러: 해당 BinaryContent는 데이터파일에 존재하지 않습니다."));

        binaryContentRepository.deleteById(binaryContentId);

        log.info("BinaryContent: {}가 삭제됨.", binaryContentTemp.getId());
    }

}
