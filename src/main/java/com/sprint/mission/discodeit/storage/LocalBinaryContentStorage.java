package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
@Slf4j
public class LocalBinaryContentStorage implements BinaryContentStorage {

  private final Path root;

  public LocalBinaryContentStorage(@Value("${discodeit.storage.local.root-path}") Path root) {
    this.root = root.toAbsolutePath().normalize();
  }

  @PostConstruct
  public void init() {
    try {
      Files.createDirectories(root);
      log.info("Local binary content storage initialized: {}", root);
    } catch (IOException e) {
      throw new RuntimeException("Failed to initialize local binary content storage: " + root, e);
    }
  }

  private Path resolvePath(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("File id must not be null.");
    }

    Path resolvedPath = root.resolve(id.toString()).normalize();
    if (!resolvedPath.startsWith(root)) {
      throw new IllegalArgumentException("Invalid file path.");
    }
    return resolvedPath;
  }

  @Override
  public UUID put(UUID id, byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      throw new IllegalArgumentException("File bytes must not be empty.");
    }

    Path targetPath = resolvePath(id);

    try {
      Thread.sleep(3000);
      Files.write(targetPath, bytes);
      log.info("File saved: {} ({} bytes)", targetPath, bytes.length);
      return id;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Thread interrupted while simulating delay", e);
    } catch (IOException e) {
      throw new RuntimeException("Failed to save file: " + id, e);
    }
  }

  @Override
  public InputStream get(UUID id) {
    Path requested = resolvePath(id);
    if (!Files.exists(requested) || Files.isDirectory(requested)) {
      throw new NoSuchElementException("File not found: " + id);
    }

    try {
      return Files.newInputStream(requested);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read file: " + id, e);
    }
  }

  @Override
  public ResponseEntity<Resource> download(BinaryContentResponse binaryContentResponse) {
    byte[] bytes;
    try (InputStream inputStream = get(binaryContentResponse.id())) {
      bytes = inputStream.readAllBytes();
    } catch (IOException e) {
      throw new RuntimeException("Failed to download file: " + binaryContentResponse.id(), e);
    }

    String encodedFileName = URLEncoder.encode(
        binaryContentResponse.fileName(),
        StandardCharsets.UTF_8
    ).replaceAll("\\+", "%20");

    MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
    if (binaryContentResponse.contentType() != null && !binaryContentResponse.contentType()
        .isBlank()) {
      mediaType = MediaType.parseMediaType(binaryContentResponse.contentType());
    }

    Resource resource = new ByteArrayResource(bytes);

    return ResponseEntity.ok()
        .contentType(mediaType)
        .contentLength(bytes.length)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename(encodedFileName, StandardCharsets.UTF_8)
                .build()
                .toString()
        )
        .body(resource);
  }
}
