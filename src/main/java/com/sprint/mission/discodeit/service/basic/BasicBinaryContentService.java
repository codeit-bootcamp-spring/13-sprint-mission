package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;

    @Override
    public BinaryContentResponse create(BinaryContentCreateRequest request) {
        BinaryContent binaryContent = new BinaryContent(request.fileName(), request.contentType(), request.size(), request.bytes());
        binaryContentRepository.save(binaryContent);
        return returnResponse(binaryContent);
    }

    @Override
    public BinaryContentResponse find(UUID id) {
        BinaryContent binaryContent = binaryContentRepository.findById(id);
        if(binaryContent==null) {
            throw new IllegalArgumentException("존재하지 않는 파일입니다.");
        }

        return returnResponse(binaryContent);
    }

    @Override
    public List<BinaryContentResponse> findAllByIdIn(List<UUID> ids) {
        List<BinaryContentResponse> responses = new ArrayList<>();
        List<BinaryContent> binaryContents = binaryContentRepository.findAll();

        for (BinaryContent binaryContent : binaryContents) {
            if(ids.contains(binaryContent.getId())) {
                responses.add(returnResponse(binaryContent));
            }
        }
        return responses;
    }

    @Override
    public void delete(UUID id) {
        BinaryContent binaryContent = binaryContentRepository.findById(id);
        if(binaryContent==null) {
            throw new IllegalArgumentException("존재하지 않는 파일입니다.");
        }

        binaryContentRepository.delete(id);
    }

    private BinaryContentResponse returnResponse(BinaryContent binaryContent) {
        return new BinaryContentResponse(binaryContent.getId(), binaryContent.getFileName(), binaryContent.getContentType(), binaryContent.getSize(), binaryContent.getBytes());
    }

}
