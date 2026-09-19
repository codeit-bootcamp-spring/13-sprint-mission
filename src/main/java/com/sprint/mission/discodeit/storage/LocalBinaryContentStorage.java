package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
public class LocalBinaryContentStorage implements BinaryContentStorage {

  private static final String STORAGE_INIT_ERROR_MESSAGE =
      "로컬 저장소 디렉토리를 생성할 수 없습니다.";

  private static final String FILE_SAVE_ERROR_MESSAGE =
      "파일 저장 실패: ";

  private static final String FILE_READ_ERROR_MESSAGE =
      "파일 읽기 실패: ";

  private static final String FILE_DOWNLOAD_ERROR_MESSAGE =
      "파일 데이터를 불러올 수 없습니다: ";

  private final Path root;

  public LocalBinaryContentStorage(@Value("${discodeit.storage.local.root-path}") String rootPath) {
    this.root = Paths.get(rootPath);
  }

  @PostConstruct
  public void init() {
    try {
      if (!Files.exists(root)) {
        Files.createDirectories(root);
      }
    } catch (IOException e) {
      throw new RuntimeException(STORAGE_INIT_ERROR_MESSAGE, e);
    }
  }

  private Path resolvePath(UUID id) {
    return root.resolve(id.toString());
  }

  @Override
  public UUID put(UUID id, byte[] data) {
    Path filePath = resolvePath(id);
    try {
      Files.write(filePath, data);
      log.info("파일 로컬 저장 성공 - fileId: {}, path: {}", id, filePath);
    } catch (IOException e) {
      log.error("파일 저장 실패 - fileId: {}", id, e);
      throw new RuntimeException(FILE_SAVE_ERROR_MESSAGE + id, e);
    }

    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCompletion(int status) {
          if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
            try {
              Files.deleteIfExists(filePath);
              log.info("트랜잭션 롤백으로 인한 로컬 파일 삭제 완료 - path: {}", filePath);
            } catch (IOException e) {
              log.error("롤백에 의한 파일 삭제 실패: {}", filePath, e);
            }
          }
        }
      });
    }

    return id;
  }

  @Override
  public InputStream get(UUID id) {
    try {
      Path filePath = resolvePath(id);
      return Files.newInputStream(filePath);
    } catch (IOException e) {
      log.error("파일 읽기 실패 - fileId: {}", id, e);
      throw new RuntimeException(FILE_READ_ERROR_MESSAGE + id, e);
    }
  }

  @Override
  public ResponseEntity<Resource> download(BinaryContentDto dto) {
    try {
      InputStream inputStream = get(dto.id());
      Resource resource = new InputStreamResource(inputStream);

      String encodedFileName = java.net.URLEncoder.encode(dto.fileName(),
              java.nio.charset.StandardCharsets.UTF_8)
          .replace("+", "%20");

      return org.springframework.http.ResponseEntity.ok()
          .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
              "attachment; filename*=UTF-8''" + encodedFileName)
          .contentType(org.springframework.http.MediaType.parseMediaType(dto.contentType()))
          .contentLength(dto.size())
          .body(resource);

    } catch (Exception e) {
      throw new RuntimeException(FILE_DOWNLOAD_ERROR_MESSAGE + dto.id(), e);
    }
  }
}