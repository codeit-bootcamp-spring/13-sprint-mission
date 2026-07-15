package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(name = "binary_contents")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BinaryContent extends BaseEntity {

    // 파일 이름
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    // 파일 타입
    // 예: image/png, image/jpeg, application/pdf
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    // 파일 크기
    @Column(name = "size", nullable = false)
    private Long size;

    /*
     * 심화 요구사항 기준 생성자
     *
     * 실제 byte[]는 DB에 저장하지 않고,
     * LocalBinaryContentStorage 같은 별도 저장소에 저장한다.
     */
    public BinaryContent(String fileName, String contentType, Long size) {
        validate(fileName, contentType, size);

        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
    }

    /*
     * 기존 코드 호환용 생성자
     *
     * 기존 서비스 코드에서 new BinaryContent(fileName, contentType, bytes)를
     * 호출하고 있을 수 있으므로 일단 컴파일 호환을 위해 유지한다.
     *
     * 단, bytes 자체는 엔티티에 저장하지 않고 size 계산에만 사용한다.
     */
    public BinaryContent(String fileName, String contentType, byte[] bytes) {
        validate(fileName, contentType, bytes);

        this.fileName = fileName;
        this.contentType = contentType;
        this.size = (long) bytes.length;
    }

    /*
     * BinaryContent는 BaseEntity만 상속하므로 updatedAt을 실제 필드로 가지지 않는다.
     * 기존 BinaryContentResponse 생성 코드 호환용으로 null을 반환한다.
     */
    public Instant getUpdatedAt() {
        return null;
    }

    private void validate(String fileName, String contentType, Long size) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("파일 이름은 비어 있을 수 없습니다.");
        }

        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("파일 타입은 비어 있을 수 없습니다.");
        }

        if (size == null || size <= 0) {
            throw new IllegalArgumentException("파일 크기는 0보다 커야 합니다.");
        }
    }

    private void validate(String fileName, String contentType, byte[] bytes) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("파일 이름은 비어 있을 수 없습니다.");
        }

        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("파일 타입은 비어 있을 수 없습니다.");
        }

        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("파일 데이터는 비어 있을 수 없습니다.");
        }
    }
}