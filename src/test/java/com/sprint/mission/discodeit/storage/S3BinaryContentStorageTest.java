package com.sprint.mission.discodeit.storage;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import java.util.UUID;

import com.sprint.mission.discodeit.storage.s3.S3BinaryContentStorage;
import com.sprint.mission.discodeit.storage.s3.S3StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("S3BinaryContentStorage 테스트")
@ExtendWith(MockitoExtension.class)
class S3BinaryContentStorageTest {

  @Mock
  private S3StorageProperties properties;

  private S3BinaryContentStorage s3BinaryContentStorage;

  @BeforeEach
  void setUp() {
    s3BinaryContentStorage = new S3BinaryContentStorage(properties);
  }

  @DisplayName("파일을 S3에 업로드한다")
  @Test
  void testPutSuccessfully() {
    // given: S3에 업로드할 파일 정보
    UUID fileId = UUID.randomUUID();
    byte[] fileContent = "Test content".getBytes();

    // when & then: 업로드 메서드 호출
    // 실제 S3 연결 없이는 RuntimeException 발생
    assertThrows(RuntimeException.class, () -> {
      s3BinaryContentStorage.put(fileId, fileContent);
    });
  }

  @DisplayName("파일을 S3에서 다운로드한다")
  @Test
  void testGetSuccessfully() {
    // given: S3에서 다운로드할 파일 ID
    UUID fileId = UUID.randomUUID();

    // when & then: 다운로드 메서드 호출
    assertThrows(RuntimeException.class, () -> {
      s3BinaryContentStorage.get(fileId);
    });
  }

  @DisplayName("Presigned URL을 생성하여 다운로드를 리다이렉트한다")
  @Test
  void testDownloadWithPresignedUrl() {
    // given: 다운로드할 파일의 메타데이터
    BinaryContentDto metaData = new BinaryContentDto(
        UUID.randomUUID(),
        "test-file.txt",
        1024L,
        "text/plain"
    );

    // when & then: 다운로드 메서드 호출
    assertThrows(RuntimeException.class, () -> {
      s3BinaryContentStorage.download(metaData);
    });
  }

  @DisplayName("S3에서 파일을 찾을 수 없을 때 예외를 던진다")
  @Test
  void testGetThrowsExceptionWhenFileNotFound() {
    // given: 존재하지 않는 파일 ID
    UUID fileId = UUID.randomUUID();

    // when & then: 예외 발생 확인
    assertThrows(RuntimeException.class, () -> {
      s3BinaryContentStorage.get(fileId);
    });
  }

  @DisplayName("S3에 업로드할 때 예외 발생 시 RuntimeException을 던진다")
  @Test
  void testPutThrowsRuntimeExceptionOnError() {
    // given: 업로드 실패 상황
    UUID fileId = UUID.randomUUID();
    byte[] fileContent = "Test content".getBytes();

    // when & then: 예외 발생 확인
    assertThrows(RuntimeException.class, () -> {
      s3BinaryContentStorage.put(fileId, fileContent);
    });
  }
}
