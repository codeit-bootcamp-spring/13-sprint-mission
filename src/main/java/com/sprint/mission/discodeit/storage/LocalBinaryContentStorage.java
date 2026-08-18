package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.BinaryContentDownloadResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
@ConditionalOnProperty(
        name = "discodeit.storage.type",
        havingValue = "local",
        matchIfMissing = true
)
public class LocalBinaryContentStorage implements BinaryContentStorage {

    private final Path rootPath;

    public LocalBinaryContentStorage(
            @Value(
                    "${discodeit.storage.local.root-path:"
                            + "data/binaryContents}"
            )
            String rootPath
    ) {
        this.rootPath = Path.of(rootPath);
    }

    /**
     * 애플리케이션 시작 시 로컬 저장소 디렉터리를 생성한다.
     */
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new RuntimeException(
                    "바이너리 콘텐츠 저장소 디렉토리 생성 중 "
                            + "오류가 발생했습니다.",
                    e
            );
        }
    }

    /**
     * 바이너리 데이터를 로컬 파일 시스템에 저장한다.
     */
    @Override
    public void put(UUID id, byte[] bytes) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "저장할 바이너리 콘텐츠 id는 필수입니다."
            );
        }

        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException(
                    "저장할 바이너리 데이터는 비어 있을 수 없습니다."
            );
        }

        try {
            Files.write(resolvePath(id), bytes);
        } catch (IOException e) {
            throw new RuntimeException(
                    "바이너리 콘텐츠 파일 저장 중 오류가 발생했습니다. "
                            + "id=" + id,
                    e
            );
        }
    }

    /**
     * UUID에 해당하는 바이너리 데이터를 조회한다.
     */
    @Override
    public byte[] get(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "조회할 바이너리 콘텐츠 id는 필수입니다."
            );
        }

        Path path = resolvePath(id);

        if (!Files.exists(path)) {
            throw new IllegalStateException(
                    "바이너리 콘텐츠 파일을 찾을 수 없습니다. id=" + id
            );
        }

        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(
                    "바이너리 콘텐츠 파일 조회 중 오류가 발생했습니다. "
                            + "id=" + id,
                    e
            );
        }
    }

    /**
     * UUID에 해당하는 로컬 파일을 삭제한다.
     */
    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "삭제할 바이너리 콘텐츠 id는 필수입니다."
            );
        }

        try {
            Files.deleteIfExists(resolvePath(id));
        } catch (IOException e) {
            throw new RuntimeException(
                    "바이너리 콘텐츠 파일 삭제 중 오류가 발생했습니다. "
                            + "id=" + id,
                    e
            );
        }
    }

    /**
     * 로컬 저장소에서는 파일 바이트를 직접 HTTP 응답으로 반환한다.
     */
    @Override
    public ResponseEntity<?> download(
            UUID id,
            BinaryContentDownloadResponse response
    ) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "다운로드할 바이너리 콘텐츠 id는 필수입니다."
            );
        }

        if (response == null) {
            throw new IllegalArgumentException(
                    "다운로드할 바이너리 콘텐츠 정보는 필수입니다."
            );
        }

        byte[] bytes = response.getBytes();

        if (bytes == null) {
            throw new IllegalStateException(
                    "다운로드할 바이너리 콘텐츠 데이터가 없습니다."
            );
        }

        return ResponseEntity.ok()
                .contentType(resolveMediaType(response.getContentType()))
                .contentLength(bytes.length)
                .headers(headers -> headers.setContentDisposition(
                        ContentDisposition.inline()
                                .filename(
                                        resolveFileName(response.getFileName()),
                                        StandardCharsets.UTF_8
                                )
                                .build()
                ))
                .body(bytes);
    }

    /**
     * UUID를 로컬 파일 경로로 변환한다.
     */
    private Path resolvePath(UUID id) {
        return rootPath.resolve(id.toString());
    }

    /**
     * Content-Type이 없으면 기본 바이너리 타입을 사용한다.
     */
    private MediaType resolveMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    /**
     * 파일 이름이 없으면 기본 파일 이름을 사용한다.
     */
    private String resolveFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "download";
        }

        return fileName;
    }
}