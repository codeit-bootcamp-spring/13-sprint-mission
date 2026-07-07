package com.sprint.mission.discodeit.service.basic;


import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.JPABinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {
    private final JPABinaryContentRepository binaryContentRepository;
    private final BinaryContentStorage binaryContentStorage;
    private final BinaryContentMapper binaryContentMapper;

    @Override
    public BinaryContentDto findByID(UUID id){
        BinaryContent bc = binaryContentRepository.findById(id).stream().findFirst().orElseThrow(
                () -> new DiscodeitException("Content not existed ","BinaryContent",404)
        );

        return binaryContentMapper.toDto(bc,getDataFromId(id));
    }

    @Override
    public List<BinaryContentDto> findAllByIdIn(List<UUID> ids){
        return ids.stream()
                .map(id -> {
                    BinaryContent bc = binaryContentRepository.findById(id).orElse(null);
                    if (bc != null){
                        return binaryContentMapper.toDto(bc,getDataFromId(id));
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional
    public void delete(UUID id){
        binaryContentRepository.deleteById(id);
    }

    private byte[] getDataFromId(UUID id){
        byte[] data;
        try{
            InputStream in = binaryContentStorage.get(id);
            data = in.readAllBytes();
            in.close();
        } catch(IOException e){
            throw new RuntimeException(e);
        }
        return data;
    }

}
