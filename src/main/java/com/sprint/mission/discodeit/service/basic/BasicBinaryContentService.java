package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.file.FileStorageException;
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
        log.debug("파일 업로드 시작");

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
            log.error("첨부 파일 업로드 실패");
            throw new FileStorageException(request.file().getOriginalFilename());
        }

        log.info("파일 업로드 완료");

        return binaryContentMapper.toDto(binaryContent);
    }

    @Override
    @Transactional(readOnly = true)
    public BinaryContentDto getBinaryContent(UUID binaryContentId) {
        //BinaryContent 검색
        BinaryContent binaryContentTemp = binaryContentRepository.findById(binaryContentId)
                .orElseThrow(() -> new BinaryContentNotFoundException(binaryContentId));

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
                .orElseThrow(() -> new BinaryContentNotFoundException(binaryContentId));

        binaryContentRepository.deleteById(binaryContentId);

        log.info("BinaryContent: {}가 삭제됨.", binaryContentTemp.getId());
    }

}
