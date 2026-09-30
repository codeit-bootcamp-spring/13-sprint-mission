package com.sprint.mission.discodeit.repository.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.discodeit.dto.request.MultipartFileDto;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BaseEntity;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.QBinaryContent;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BinaryContentQueryDslImpl implements BinaryContentQueryDsl {

    private final JPAQueryFactory jpaQueryFactory;

    private final EntityManager entitymanager;

    private final BinaryContentStorage storage;

    private final QBinaryContent binaryContent = QBinaryContent.binaryContent;

    @RequiredArgsConstructor
    @Getter
    public static class QueryDto{
        public final UUID id;
        public final String filename;
        public final Long size;
        public final String contentType;
        public byte[] bytes;
    }


    /*
    저장 관련 매서드.
    local, S3 에 실제 데이터를 저장하는 역할도 포함한다.
     */

    @Override
    public BinaryContent saveWithMultipartCommand(MultipartFileDto command){
        if (command == null) return null;
        BinaryContent metadata = new BinaryContent(
                command.filename(),
                command.contentType(),
                command.size()
        );

        save(metadata);

        // save data DB contents with command Id.
        // todo - 외부 I/O 이므로 비동기 전환.
        // todo - 바이너리 데이터 저장 실패 시, 재시도 and 저장된 메타데이터 삭제 로직
        storage.put(metadata.getId(),command.content());

        return metadata;
    }

    public List<BinaryContent> saveAllFromMultipartFileDtoList(List<MultipartFileDto> files){
        return files.stream().map(this::saveWithMultipartCommand).toList();
    }

    private <T extends BaseEntity> void save(T entity){
        if(entity.getId() == null) entitymanager.persist(entity);
        else entitymanager.merge(entity);
    }


    // find method
    @Override
    public Optional<BinaryContentDto> getBinaryContentById(UUID id){
        return id == null
                ? Optional.empty()
                : convert(query(binaryContent.id.eq(id))).values().stream().findFirst();
    }

    @Override
    public Map<UUID,BinaryContentDto> getBinaryContentsInIdList(List<UUID> list){
        return list.isEmpty()
                ? new HashMap<>()
                : convert(query(binaryContent.id.in(list)));
    }

    @Override
    public void deleteBinaryContent(BinaryContent target){
        storage.delete(target.getId());
        entitymanager.remove(target);
    }

    /*
    storage or db method
     */

    private byte[] getData(UUID id){
        // Local Storage will return upcasted BufferedStream
        try (BufferedInputStream stream = (BufferedInputStream) storage.get(id)) {
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /*
    query string, expression method
     */

    private List<QueryDto> query(BooleanExpression... exps){
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                QueryDto.class,
                                binaryContent.id,
                                binaryContent.fileName,
                                binaryContent.size,
                                binaryContent.contentType
                        )
                )
                .from(binaryContent)
                .where(condition(exps))
                .fetch();
    }

    private BooleanBuilder condition(BooleanExpression... exps){
        BooleanBuilder where = new BooleanBuilder();

        for(BooleanExpression exp : exps){
            where.and(exp);
        }

        return where;
    }

    /*
    query -> dto convert method.
     */

    private Map<UUID,BinaryContentDto> convert(List<QueryDto> result){
        return result.stream().collect(
                    Collectors.toMap(
                            QueryDto::getId,
                            this::toDto
                    )
        );
    }

    private BinaryContentDto toDto(QueryDto dto){
        return new BinaryContentDto(
                dto.id,
                dto.filename,
                dto.size,
                dto.contentType,
                getData(dto.id)
        );
    }



}
