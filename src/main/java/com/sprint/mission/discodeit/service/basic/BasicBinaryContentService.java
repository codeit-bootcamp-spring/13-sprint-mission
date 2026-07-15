package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import com.sprint.mission.discodeit.storage.*;
import lombok.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository repository;
    private final BinaryContentStorage binaryContentStorage;

    @Override
    @Transactional
    public BinaryContentResponse create(CreateBinaryContentRequest request) {
        if (request == null)  {
            throw new IllegalArgumentException("바이너리 콘텐츠 생성 요청은 필수입니다.");
        }
        BinaryContent binaryContent = new BinaryContent(
                request.fileName(),
                (long)request.bytes().length,
                request.contentType()
        );

        repository.save(binaryContent);
        binaryContentStorage.put(binaryContent.getId(), request.bytes());
        return BinaryContentResponse.from(binaryContent);
    }

    @Override
    public BinaryContentResponse find(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("파일 아이디를 찾을 수 없습니다.");
        }

        BinaryContent binaryContent = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

        return BinaryContentResponse.from(binaryContent);
    }

    @Override
    public List<BinaryContentResponse> findAllByIdIn(List<UUID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("바이너리 콘텐츠 ID 목록은 필수입니다.");
        }
        return  repository.findAllByIdIn(ids)
                .stream()
                .map(BinaryContentResponse::from)
                .toList();

    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("바이너리 콘텐츠 ID는 필수입니다.");
        }

        BinaryContent binaryContent = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 바이너리 콘텐츠입니다."));

        repository.delete(binaryContent);
    }

}
