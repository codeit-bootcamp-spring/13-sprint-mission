package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentDeleteException;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentDownloadException;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentUploadException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "discodeit.storage",
    name = "discodeit.storage.type",
    havingValue = "local",
    matchIfMissing = true
)
public class LocalBinaryContentStorage implements BinaryContentStorage {

  private final Path root;


  public LocalBinaryContentStorage(
      @Value("${discodeit.storage.local.root-path}") String rootPath
  ) {
    this.root = Paths.get(rootPath);
  }

  private Path resolvePath(UUID id) {
    return root.resolve(id.toString());
  }

  @PostConstruct
  public void init() throws IOException {
    Files.createDirectories(root);
  }

  @Override
  public UUID put(UUID id, byte[] bytes) {
    Path path = resolvePath(id);

    try {
      Files.write(path, bytes);
    } catch (IOException e) {
      throw new BinaryContentUploadException(id, e);
    }
    return id;
  }

  @Override
  public InputStream get(UUID id) {
    Path path = resolvePath(id);

    try {
      return Files.newInputStream(path);
    } catch (IOException e) {
      throw new BinaryContentDownloadException(id, e);
    }
  }

  @Override
  public void delete(UUID id) {
    Path path = resolvePath(id);

    try {
      boolean deleted = Files.deleteIfExists(path);

      if (deleted) {
        log.debug("로컬 파일 삭제 완료: binaryContentId={}", id);
      } else {
        log.warn("삭제할 로컬 파일이 존재하지 않음: binaryContentId={}", id);
      }
    } catch (IOException e) {
      log.error("로컬 파일 삭제 실패: binaryContentId={}", id, e);
      throw new BinaryContentDeleteException(id, e);
    }
  }

  @Override
  public ResponseEntity<?> download(BinaryContentDto binaryContentDto) {
    InputStream inputStream = get(binaryContentDto.id());
    Resource resource = new InputStreamResource(inputStream);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + binaryContentDto.fileName() + "\""
        )
        .contentType(MediaType.parseMediaType(binaryContentDto.contentType()))
        .body(resource);
  }
}
