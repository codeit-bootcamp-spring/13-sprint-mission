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
        //입력값 검증 처리하겠습니다
//        validateString(request.contentPath());

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
        //입력값 검증 처리하겠습니다
//        validateUUID(binaryContentId);

        //BinaryContent 검색
        BinaryContent binaryContentTemp = binaryContentRepository.findBinaryContentById(binaryContentId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 BinaryContent는 데이터파일에 존재하지 않습니다."));

        return binaryContentTemp;
    }

    @Override
    public List<BinaryContent> findAllBinaryContentByIdIn(List<UUID> binaryContentIds) {
        //입력값 검증 처리하겠습니다
//        validateList(binaryContentIds);
        for (UUID binaryContentId : binaryContentIds) {
//            validateUUID(binaryContentId);
        }

        //BinaryContent들 검색
        List<BinaryContent> binaryContentList = binaryContentRepository.findAllBinaryContentByIdIn(binaryContentIds);

        return binaryContentList;
    }

    @Override
    public void deleteBinaryContent(UUID binaryContentId) {
        //입력값 검증 처리하겠습니다
//        validateUUID(binaryContentId);

        //BinaryContent 검색
        BinaryContent binaryContentTemp = binaryContentRepository.findBinaryContentById(binaryContentId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 BinaryContent는 데이터파일에 존재하지 않습니다."));

        binaryContentRepository.deleteBinaryContent(binaryContentId);

        log.info("BinaryContent: {}가 삭제됨.", binaryContentTemp.getId());
    }


    // 들어온 String 필드가 null 혹은 공백인지 검증하는 메서드
    private void validateString(String str) {
        if (str == null || str.isBlank()) {
            throw new IllegalArgumentException("에러: 입력값이 Null 또는 공백입니다.");
        }
    }

    // 들어온 UUID 필드가 null인지 검증하는 메서드
    private void validateUUID(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("에러: 입력값이 Null입니다.");
        }
    }
    // 들어온 List<UUID> 필드가 null인지 검증하는 메서드
    private void validateList(List<UUID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("에러: 입력값이 Null입니다.");
        }
    }
}
