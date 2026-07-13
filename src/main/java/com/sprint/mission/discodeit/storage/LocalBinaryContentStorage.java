package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "discodeit.storage", name = "type", havingValue = "local")
public class LocalBinaryContentStorage implements BinaryContentStorage {

  private final Path root; // 로컬 디스크 루트 경로

  public LocalBinaryContentStorage(@Value("${discodeit.storage.local.root-path}") Path root) {
    this.root = root.toAbsolutePath().normalize();
  }

  @PostConstruct // Bean 생성되면 자동으로 호출
  public void init() { // 루트 디렉토리 초기화
    try {
      Files.createDirectories(root);
    } catch (IOException e) {
      throw new FileStorageException("업로드 디렉터리 생성 실패: " + root, e);
    }
  }

  private Path resolvePath(UUID id) { // 파일 실제 저장 위치에 대한 규칙 정의
    return root.resolve(id.toString());
  } // 파일 저장 위치 규칙

  @Override
  public UUID put(UUID id, byte[] bytes) {
    try {
      Files.write(resolvePath(id), bytes);
      return id;
    } catch (IOException e) {
      throw new FileStorageException("저장 실패: " + id, e);
    }
  }

  @Override
  public InputStream get(UUID id) {
    try {
      return Files.newInputStream(resolvePath(id));
    } catch (IOException e) {
      throw new FileStorageException("읽기 실패: " + id, e);
    }
  }

  @Override
  public ResponseEntity<Resource> download(BinaryContentDto binaryContentDto) {
    InputStreamResource inputStreamResource = new InputStreamResource(
        get(binaryContentDto.getId())); // get 메소드 통해 바이너리 데이터 조회

    return ResponseEntity.status(HttpStatus.OK)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + binaryContentDto.getFileName() + "\"")
        .contentType(MediaType.parseMediaType(binaryContentDto.getContentType()))
        .contentLength(binaryContentDto.getSize())
        .body(inputStreamResource);
  }
}
