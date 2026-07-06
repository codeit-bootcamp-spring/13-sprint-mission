package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "discodeit.storage.type",
    havingValue = "local"
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
      throw new RuntimeException("파일 저장 실패: " + id, e);
    }
    return id;
  }

  @Override
  public InputStream get(UUID id) {
    Path path = resolvePath(id);

    try {
      return Files.newInputStream(path);
    } catch (IOException e) {
      throw new RuntimeException("파일 조회 실패: " + id, e);
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
