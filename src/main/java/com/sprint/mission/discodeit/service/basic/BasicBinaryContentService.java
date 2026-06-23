package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

//BinaryContentService 기본 구현체 (파일/이미지/바이너리 데이터를 관리하는 서비스 계층)
@RequiredArgsConstructor
@Service
public class BasicBinaryContentService implements BinaryContentService {
    private final BinaryContentRepository binaryContentRepository; //BinaryContent 저장소(추상화된 데이터 접근 계층)

    @Override //파일(BinaryContent) 생성
    public BinaryContent create(BinaryContentCreateRequest request) {
        String fileName = request.getFileName();
        byte[] bytes = request.getBytes();
        String contentType = request.getContentType();
        //엔티티 생성(UUID+creatrdAt 내부 생성)
        BinaryContent binaryContent = new BinaryContent(fileName, contentType, bytes);
        return binaryContentRepository.save(binaryContent);
    }

    @Override //단건조회
    public BinaryContent find(UUID binaryContentId) {
        return binaryContentRepository.findById(binaryContentId)
                .orElseThrow(() -> new NoSuchElementException("BinaryContent with id " + binaryContentId + " not found"));
    }

    @Override //여러 파일 조회(IN 조회)
    public List<BinaryContent> findAllByIdIn(List<UUID> binaryContentIds) {
        return binaryContentRepository.findAllByIdIn(binaryContentIds).stream()
                .toList();
    }

    @Override //삭제
    public void delete(UUID binaryContentId) {
        if (!binaryContentRepository.existsById(binaryContentId)) {
            throw new NoSuchElementException("BinaryContent with id " + binaryContentId + " not found");
        }
        binaryContentRepository.deleteById(binaryContentId);
    }
}
