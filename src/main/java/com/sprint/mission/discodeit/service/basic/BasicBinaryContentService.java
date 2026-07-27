package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.*;
import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.mapper.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import com.sprint.mission.discodeit.storage.*;
import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {
private final BinaryContentRepository repository;
    private final BinaryContentStorage binaryContentStorage;
    private final BinaryContentMapper binaryContentMapper;

    @Override
    @Transactional
    public BinaryContentDto create(CreateBinaryContentCommand command) {
        if (command == null)  {
            throw new IllegalArgumentException("바이너리 콘텐츠 생성 요청은 필수입니다.");
        }
        BinaryContent binaryContent = new BinaryContent(
                command.fileName(),
                (long)command.bytes().length,
                command.contentType()
        );

        repository.save(binaryContent);
        binaryContentStorage.put(binaryContent.getId(), command.bytes());

        return binaryContentMapper.toDto(binaryContent);
    }

    @Override
    public BinaryContentDto find(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("파일 아이디를 찾을 수 없습니다.");
        }

        BinaryContent binaryContent = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

        return binaryContentMapper.toDto(binaryContent);
    }

    @Override
    public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("바이너리 콘텐츠 ID 목록은 필수입니다.");
        }

        List<BinaryContent> binaryContents = repository.findAllByIdIn(ids);
        return binaryContentMapper.toDtoList(binaryContents);
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
        binaryContentStorage.delete(id);
    }

}
