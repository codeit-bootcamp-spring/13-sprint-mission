package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "binary_contents")
public class BinaryContent extends BaseUpdatableEntity {

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false)
    private Long size;

    @Enumerated(EnumType.STRING)
    @Column (nullable = false, length = 20)
    private BinaryContentStatus status = BinaryContentStatus.PROCESSING;

    public BinaryContent(String fileName, Long size, String contentType) {

        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
    }

    public void updateStatus(BinaryContentStatus status) {
        this.status = Objects.requireNonNull(
                status, "바이너리 콘텐츠 상태는 필수입니다."
        );
    }

}
