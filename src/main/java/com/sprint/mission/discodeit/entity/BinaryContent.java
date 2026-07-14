package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class BinaryContent extends BaseEntity {

    private UUID id;
    private Instant createdAt;
    private UUID userId;
    private UUID messageId;

    private String fileName;
    private Long size;
    private String contentType;
    private byte[] bytes;

    public BinaryContent(UUID userId, UUID messageId, String fileName) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.userId = userId;
        this.messageId = messageId;
        this.fileName = fileName;
    }

}



/*
[ ]  BinaryContent
"이미지, 파일 등 바이너리 데이터를 표현"하는 도메인 모델입니다. 사용자의 프로필 이미지, 메시지에 첨부된 파일을 저장하기 위해 활용합니다.
[ ] 수정 불가능한 도메인 모델로 간주합니다. 따라서 updatedAt 필드는 정의하지 않습니다.
[ ] User, Message 도메인 모델과의 의존 관계 방향성을 잘 고려하여 id 참조 필드를 추가하세요.
 */