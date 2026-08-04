package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentDownloadResponse;
import com.sprint.mission.discodeit.dto.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentStorageException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicBinaryContentService 단위 테스트")
class BasicBinaryContentServiceTest {

    @Mock
    private BinaryContentRepository binaryContentRepository;

    @Mock
    private BinaryContentStorage binaryContentStorage;

    @InjectMocks
    private BasicBinaryContentService binaryContentService;

    @Nested
    @DisplayName("파일 생성")
    class Create {

        @Test
        @DisplayName("정상적인 요청이면 메타데이터와 파일을 저장한다")
        void success() {
            // given
            UUID binaryContentId = UUID.randomUUID();
            byte[] bytes = "test file".getBytes();

            BinaryContentCreateRequest request =
                    org.mockito.Mockito.mock(BinaryContentCreateRequest.class);

            BinaryContent savedBinaryContent =
                    org.mockito.Mockito.mock(BinaryContent.class);

            given(request.getFileName())
                    .willReturn("test.png");
            given(request.getContentType())
                    .willReturn("image/png");
            given(request.getBytes())
                    .willReturn(bytes);

            given(savedBinaryContent.getId())
                    .willReturn(binaryContentId);
            given(savedBinaryContent.getFileName())
                    .willReturn("test.png");
            given(savedBinaryContent.getContentType())
                    .willReturn("image/png");
            given(savedBinaryContent.getSize())
                    .willReturn((long) bytes.length);
            given(savedBinaryContent.getCreatedAt())
                    .willReturn(Instant.now());
            given(savedBinaryContent.getUpdatedAt())
                    .willReturn(null);

            given(binaryContentRepository.save(
                    org.mockito.ArgumentMatchers.any(BinaryContent.class)
            )).willReturn(savedBinaryContent);

            // when
            BinaryContentResponse response =
                    binaryContentService.create(request);

            // then
            assertThat(response).isNotNull();

            verify(binaryContentRepository)
                    .save(org.mockito.ArgumentMatchers.any(BinaryContent.class));

            verify(binaryContentStorage)
                    .put(binaryContentId, bytes);
        }

        @Test
        @DisplayName("contentType이 없으면 application/octet-stream을 사용한다")
        void defaultContentType() {
            // given
            UUID binaryContentId = UUID.randomUUID();
            byte[] bytes = "binary".getBytes();

            BinaryContentCreateRequest request =
                    org.mockito.Mockito.mock(BinaryContentCreateRequest.class);

            BinaryContent savedBinaryContent =
                    org.mockito.Mockito.mock(BinaryContent.class);

            given(request.getFileName())
                    .willReturn("test.bin");
            given(request.getContentType())
                    .willReturn(null);
            given(request.getBytes())
                    .willReturn(bytes);

            given(savedBinaryContent.getId())
                    .willReturn(binaryContentId);
            given(savedBinaryContent.getFileName())
                    .willReturn("test.bin");
            given(savedBinaryContent.getContentType())
                    .willReturn("application/octet-stream");
            given(savedBinaryContent.getSize())
                    .willReturn((long) bytes.length);

            given(binaryContentRepository.save(
                    org.mockito.ArgumentMatchers.any(BinaryContent.class)
            )).willReturn(savedBinaryContent);

            // when
            BinaryContentResponse response =
                    binaryContentService.create(request);

            // then
            assertThat(response).isNotNull();

            verify(binaryContentStorage)
                    .put(binaryContentId, bytes);
        }

        @Test
        @DisplayName("생성 요청이 null이면 예외가 발생한다")
        void nullRequest() {
            assertThatThrownBy(() ->
                    binaryContentService.create(null)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(
                            "바이너리 콘텐츠 생성 요청은 비어 있을 수 없습니다"
                    );

            verify(binaryContentRepository, never())
                    .save(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("파일 이름이 비어 있으면 예외가 발생한다")
        void blankFileName() {
            // given
            BinaryContentCreateRequest request =
                    org.mockito.Mockito.mock(BinaryContentCreateRequest.class);

            given(request.getFileName())
                    .willReturn("   ");

            // when & then
            assertThatThrownBy(() ->
                    binaryContentService.create(request)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("파일 이름은 비어 있을 수 없습니다");

            verify(binaryContentRepository, never())
                    .save(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("파일 데이터가 null이면 예외가 발생한다")
        void nullBytes() {
            // given
            BinaryContentCreateRequest request =
                    org.mockito.Mockito.mock(BinaryContentCreateRequest.class);

            given(request.getFileName())
                    .willReturn("test.txt");
            given(request.getBytes())
                    .willReturn(null);

            // when & then
            assertThatThrownBy(() ->
                    binaryContentService.create(request)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("파일 데이터는 비어 있을 수 없습니다");

            verify(binaryContentRepository, never())
                    .save(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("파일 데이터가 비어 있으면 예외가 발생한다")
        void emptyBytes() {
            // given
            BinaryContentCreateRequest request =
                    org.mockito.Mockito.mock(BinaryContentCreateRequest.class);

            given(request.getFileName())
                    .willReturn("test.txt");
            given(request.getBytes())
                    .willReturn(new byte[0]);

            // when & then
            assertThatThrownBy(() ->
                    binaryContentService.create(request)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("파일 데이터는 비어 있을 수 없습니다");

            verify(binaryContentRepository, never())
                    .save(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("파일 저장소 저장에 실패하면 DB 메타데이터를 삭제한다")
        void storageFailureCleansMetadata() {
            // given
            UUID binaryContentId = UUID.randomUUID();
            byte[] bytes = "test".getBytes();

            BinaryContentCreateRequest request =
                    org.mockito.Mockito.mock(BinaryContentCreateRequest.class);

            BinaryContent savedBinaryContent =
                    org.mockito.Mockito.mock(BinaryContent.class);

            given(request.getFileName())
                    .willReturn("test.txt");
            given(request.getContentType())
                    .willReturn("text/plain");
            given(request.getBytes())
                    .willReturn(bytes);

            given(savedBinaryContent.getId())
                    .willReturn(binaryContentId);
            given(savedBinaryContent.getFileName())
                    .willReturn("test.txt");

            given(binaryContentRepository.save(
                    org.mockito.ArgumentMatchers.any(BinaryContent.class)
            )).willReturn(savedBinaryContent);

            doThrow(new RuntimeException("storage error"))
                    .when(binaryContentStorage)
                    .put(binaryContentId, bytes);

            // when & then
            assertThatThrownBy(() ->
                    binaryContentService.create(request)
            )
                    .isInstanceOf(BinaryContentStorageException.class);

            verify(binaryContentRepository)
                    .deleteById(binaryContentId);
        }

        @Test
        @DisplayName("DB 메타데이터 저장에 실패하면 저장소는 호출하지 않는다")
        void repositorySaveFailure() {
            // given
            BinaryContentCreateRequest request =
                    org.mockito.Mockito.mock(BinaryContentCreateRequest.class);

            given(request.getFileName())
                    .willReturn("test.txt");
            given(request.getContentType())
                    .willReturn("text/plain");
            given(request.getBytes())
                    .willReturn("test".getBytes());

            given(binaryContentRepository.save(
                    org.mockito.ArgumentMatchers.any(BinaryContent.class)
            )).willThrow(new RuntimeException("db error"));

            // when & then
            assertThatThrownBy(() ->
                    binaryContentService.create(request)
            )
                    .isInstanceOf(BinaryContentStorageException.class);

            verify(binaryContentStorage, never())
                    .put(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.any()
                    );
        }
    }

    @Nested
    @DisplayName("파일 조회")
    class Find {

        @Test
        @DisplayName("존재하는 파일 메타데이터를 조회한다")
        void success() {
            // given
            UUID id = UUID.randomUUID();
            BinaryContent binaryContent =
                    org.mockito.Mockito.mock(BinaryContent.class);

            given(binaryContent.getId()).willReturn(id);
            given(binaryContent.getFileName()).willReturn("test.png");
            given(binaryContent.getContentType()).willReturn("image/png");
            given(binaryContent.getSize()).willReturn(100L);
            given(binaryContent.getCreatedAt()).willReturn(Instant.now());

            given(binaryContentRepository.findById(id))
                    .willReturn(Optional.of(binaryContent));

            // when
            BinaryContentResponse response =
                    binaryContentService.find(id);

            // then
            assertThat(response).isNotNull();

            verify(binaryContentRepository).findById(id);
        }

        @Test
        @DisplayName("조회 ID가 null이면 예외가 발생한다")
        void nullId() {
            assertThatThrownBy(() ->
                    binaryContentService.find(null)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(
                            "조회할 바이너리 콘텐츠 id는 null일 수 없습니다"
                    );

            verify(binaryContentRepository, never())
                    .findById(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("존재하지 않는 파일이면 예외가 발생한다")
        void notFound() {
            // given
            UUID id = UUID.randomUUID();

            given(binaryContentRepository.findById(id))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    binaryContentService.find(id)
            )
                    .isInstanceOf(BinaryContentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("파일 다운로드")
    class FindForDownload {

        @Test
        @DisplayName("파일 메타데이터와 실제 데이터를 조회한다")
        void success() {
            // given
            UUID id = UUID.randomUUID();
            byte[] bytes = "download-data".getBytes();

            BinaryContent binaryContent =
                    org.mockito.Mockito.mock(BinaryContent.class);

            given(binaryContent.getId()).willReturn(id);
            given(binaryContent.getFileName()).willReturn("download.txt");
            given(binaryContent.getContentType()).willReturn("text/plain");
            given(binaryContent.getSize()).willReturn((long) bytes.length);

            given(binaryContentRepository.findById(id))
                    .willReturn(Optional.of(binaryContent));

            given(binaryContentStorage.get(id))
                    .willReturn(bytes);

            // when
            BinaryContentDownloadResponse response =
                    binaryContentService.findForDownload(id);

            // then
            assertThat(response).isNotNull();

            verify(binaryContentStorage).get(id);
        }

        @Test
        @DisplayName("저장소 조회에 실패하면 저장소 예외가 발생한다")
        void storageFailure() {
            // given
            UUID id = UUID.randomUUID();

            BinaryContent binaryContent =
                    org.mockito.Mockito.mock(BinaryContent.class);

            given(binaryContent.getId()).willReturn(id);
            given(binaryContent.getFileName()).willReturn("test.txt");

            given(binaryContentRepository.findById(id))
                    .willReturn(Optional.of(binaryContent));

            given(binaryContentStorage.get(id))
                    .willThrow(new RuntimeException("storage error"));

            // when & then
            assertThatThrownBy(() ->
                    binaryContentService.findForDownload(id)
            )
                    .isInstanceOf(BinaryContentStorageException.class);
        }

        @Test
        @DisplayName("존재하지 않는 파일을 다운로드하면 예외가 발생한다")
        void notFound() {
            // given
            UUID id = UUID.randomUUID();

            given(binaryContentRepository.findById(id))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    binaryContentService.findForDownload(id)
            )
                    .isInstanceOf(BinaryContentNotFoundException.class);

            verify(binaryContentStorage, never())
                    .get(org.mockito.ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("파일 목록 조회")
    class FindAllByIdIn {

        @Test
        @DisplayName("ID 목록으로 파일 메타데이터를 조회한다")
        void success() {
            // given
            UUID firstId = UUID.randomUUID();
            UUID secondId = UUID.randomUUID();

            BinaryContent first =
                    org.mockito.Mockito.mock(BinaryContent.class);

            BinaryContent second =
                    org.mockito.Mockito.mock(BinaryContent.class);

            given(first.getId()).willReturn(firstId);
            given(first.getFileName()).willReturn("first.txt");
            given(first.getContentType()).willReturn("text/plain");
            given(first.getSize()).willReturn(10L);

            given(second.getId()).willReturn(secondId);
            given(second.getFileName()).willReturn("second.png");
            given(second.getContentType()).willReturn("image/png");
            given(second.getSize()).willReturn(20L);

            List<UUID> ids = List.of(firstId, secondId);

            given(binaryContentRepository.findAllByIdIn(ids))
                    .willReturn(List.of(first, second));

            // when
            List<BinaryContentResponse> responses =
                    binaryContentService.findAllByIdIn(ids);

            // then
            assertThat(responses).hasSize(2);

            verify(binaryContentRepository)
                    .findAllByIdIn(ids);
        }

        @Test
        @DisplayName("ID 목록이 null이면 빈 목록을 반환한다")
        void nullIds() {
            List<BinaryContentResponse> responses =
                    binaryContentService.findAllByIdIn(null);

            assertThat(responses).isEmpty();

            verify(binaryContentRepository, never())
                    .findAllByIdIn(
                            org.mockito.ArgumentMatchers.anyList()
                    );
        }

        @Test
        @DisplayName("ID 목록이 비어 있으면 빈 목록을 반환한다")
        void emptyIds() {
            List<BinaryContentResponse> responses =
                    binaryContentService.findAllByIdIn(List.of());

            assertThat(responses).isEmpty();

            verify(binaryContentRepository, never())
                    .findAllByIdIn(
                            org.mockito.ArgumentMatchers.anyList()
                    );
        }
    }

    @Nested
    @DisplayName("파일 삭제")
    class Delete {

        @Test
        @DisplayName("DB 메타데이터와 저장소 파일을 삭제한다")
        void success() {
            // given
            UUID id = UUID.randomUUID();

            BinaryContent binaryContent =
                    org.mockito.Mockito.mock(BinaryContent.class);

            given(binaryContent.getId()).willReturn(id);
            given(binaryContent.getFileName()).willReturn("delete.txt");

            given(binaryContentRepository.findById(id))
                    .willReturn(Optional.of(binaryContent));

            // when
            binaryContentService.delete(id);

            // then
            verify(binaryContentRepository)
                    .delete(binaryContent);

            verify(binaryContentStorage)
                    .delete(id);
        }

        @Test
        @DisplayName("삭제 ID가 null이면 예외가 발생한다")
        void nullId() {
            assertThatThrownBy(() ->
                    binaryContentService.delete(null)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(
                            "삭제할 바이너리 콘텐츠 id는 null일 수 없습니다"
                    );

            verify(binaryContentRepository, never())
                    .findById(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("존재하지 않는 파일을 삭제하면 예외가 발생한다")
        void notFound() {
            // given
            UUID id = UUID.randomUUID();

            given(binaryContentRepository.findById(id))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    binaryContentService.delete(id)
            )
                    .isInstanceOf(BinaryContentNotFoundException.class);

            verify(binaryContentStorage, never())
                    .delete(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("저장소 삭제에 실패하면 저장소 예외가 발생한다")
        void storageFailure() {
            // given
            UUID id = UUID.randomUUID();

            BinaryContent binaryContent =
                    org.mockito.Mockito.mock(BinaryContent.class);

            given(binaryContent.getId()).willReturn(id);
            given(binaryContent.getFileName()).willReturn("delete.txt");

            given(binaryContentRepository.findById(id))
                    .willReturn(Optional.of(binaryContent));

            doThrow(new RuntimeException("delete error"))
                    .when(binaryContentStorage)
                    .delete(id);

            // when & then
            assertThatThrownBy(() ->
                    binaryContentService.delete(id)
            )
                    .isInstanceOf(BinaryContentStorageException.class);

            verify(binaryContentRepository)
                    .delete(binaryContent);
        }
    }
}