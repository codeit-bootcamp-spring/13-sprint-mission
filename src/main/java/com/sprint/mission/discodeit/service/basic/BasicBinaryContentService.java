package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.FileException;
import com.sprint.mission.discodeit.exception.ObjectNotFoundException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicBinaryContentService implements BinaryContentService {

    //필드
    private final BinaryContentRepository binaryContentRepository;

    //interface
    @Override
    public BinaryContent createBinaryContent(BinaryContentCreateRequest request) {
        BinaryContent binaryContent;
        try {
            //binaryContent 생성
            binaryContent = new BinaryContent(
                    request.file().getOriginalFilename(),
                    (long) request.file().getBytes().length,
                    request.file().getContentType(),
                    request.file().getBytes()
            );
            binaryContentRepository.createBinaryContent(binaryContent);
            log.info("BinaryContent가 생성됨.");

        } catch (IOException e) {
            throw new FileException(e.getMessage());
        }

        return binaryContent;
    }

    @Override
    public BinaryContent findBinaryContentById(UUID binaryContentId) {
        //BinaryContent 검색
        BinaryContent binaryContentTemp = binaryContentRepository.findBinaryContentById(binaryContentId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 BinaryContent는 데이터파일에 존재하지 않습니다."));

        return binaryContentTemp;
    }

    @Override
    public List<BinaryContent> findAllBinaryContentByIdIn(List<UUID> binaryContentIds) {
        //BinaryContent들 검색
        List<BinaryContent> binaryContentList = binaryContentRepository.findAllBinaryContentByIdIn(binaryContentIds);

        return binaryContentList;
    }

    @Override
    public void deleteBinaryContent(UUID binaryContentId) {
        //BinaryContent 검색
        BinaryContent binaryContentTemp = binaryContentRepository.findBinaryContentById(binaryContentId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 BinaryContent는 데이터파일에 존재하지 않습니다."));

        binaryContentRepository.deleteBinaryContent(binaryContentId);

        log.info("BinaryContent: {}가 삭제됨.", binaryContentTemp.getId());
    }

}
