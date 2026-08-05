package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.file.FileStorageException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BasicBinaryContentServiceTest {

    @Mock
    private BinaryContentRepository binaryContentRepository;

    @Mock
    private BinaryContentMapper binaryContentMapper;

    @Mock
    private BinaryContentStorage binaryContentStorage;

    @InjectMocks
    private BasicBinaryContentService binaryContentService;

    @Nested
    @DisplayName("BinaryContent 생성")
    class CreateBinaryContent {

        @Test
        @DisplayName("파일 정보와 데이터를 저장하고 DTO를 반환한다")
        void create_success() throws IOException {
            // given
            byte[] bytes = "file-data".getBytes();

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.txt",
                    "text/plain",
                    bytes
            );

            BinaryContentCreateRequest request = new BinaryContentCreateRequest(file);

            UUID binaryContentId = UUID.randomUUID();

            BinaryContent savedBinaryContent = mock(BinaryContent.class);

            BinaryContentDto expected = mock(BinaryContentDto.class);

            given(binaryContentRepository.save(any(BinaryContent.class)))
                    .willReturn(savedBinaryContent);

            given(savedBinaryContent.getId())
                    .willReturn(binaryContentId);

            given(binaryContentMapper.toDto(savedBinaryContent))
                    .willReturn(expected);

            // when
            BinaryContentDto result = binaryContentService.createBinaryContent(request);

            // then
            assertThat(result)
                    .isSameAs(expected);

            ArgumentCaptor<BinaryContent> captor = ArgumentCaptor.forClass(BinaryContent.class);

            then(binaryContentRepository).should()
                    .save(captor.capture());

            BinaryContent created = captor.getValue();

            assertThat(created.getFileName())
                    .isEqualTo("test.txt");
            assertThat(created.getSize())
                    .isEqualTo(bytes.length);
            assertThat(created.getContentType())
                    .isEqualTo("text/plain");

            then(binaryContentStorage).should()
                    .put(binaryContentId, bytes);

            then(binaryContentMapper).should()
                    .toDto(savedBinaryContent);
        }

        @Test
        @DisplayName("파일 데이터를 읽지 못하면 FileStorageException이 발생한다")
        void create_fail() throws IOException {
            // given
            MultipartFile file = mock(MultipartFile.class);

            given(file.getOriginalFilename())
                    .willReturn("broken.txt");

            given(file.getBytes())
                    .willThrow(new IOException("read fail"));

            BinaryContentCreateRequest request = new BinaryContentCreateRequest(file);

            // when & then
            assertThatThrownBy(() -> binaryContentService.createBinaryContent(request))
                    .isInstanceOf(FileStorageException.class);

            then(binaryContentRepository).should(never())
                    .save(any(BinaryContent.class));
        }
    }

    @Nested
    @DisplayName("BinaryContent 단건 조회")
    class GetBinaryContent {

        @Test
        @DisplayName("BinaryContent가 존재하면 DTO로 반환한다")
        void get_success() {
            // given
            UUID binaryContentId = UUID.randomUUID();

            BinaryContent binaryContent = mock(BinaryContent.class);

            BinaryContentDto expected = mock(BinaryContentDto.class);

            given(binaryContentRepository.findById(binaryContentId))
                    .willReturn(Optional.of(binaryContent));

            given(binaryContentMapper.toDto(binaryContent))
                    .willReturn(expected);

            // when
            BinaryContentDto result = binaryContentService.getBinaryContent(binaryContentId);

            // then
            assertThat(result)
                    .isSameAs(expected);

            then(binaryContentRepository).should()
                    .findById(binaryContentId);

            then(binaryContentMapper).should()
                    .toDto(binaryContent);
        }

        @Test
        @DisplayName("BinaryContent가 존재하지 않으면 예외가 발생한다")
        void get_fail() {
            // given
            UUID binaryContentId = UUID.randomUUID();

            given(binaryContentRepository.findById(binaryContentId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> binaryContentService.getBinaryContent(binaryContentId))
                    .isInstanceOf(BinaryContentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("BinaryContent 목록 조회")
    class GetBinaryContentsByIdIn {

        @Test
        @DisplayName("ID 목록에 해당하는 BinaryContent를 DTO 목록으로 반환한다")
        void get_success() {
            // given
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            List<UUID> ids = List.of(id1, id2);

            BinaryContent content1 = mock(BinaryContent.class);
            BinaryContent content2 = mock(BinaryContent.class);

            BinaryContentDto dto1 = mock(BinaryContentDto.class);
            BinaryContentDto dto2 = mock(BinaryContentDto.class);

            given(binaryContentRepository.findAllByIdIn(ids))
                    .willReturn(List.of(content1, content2));

            given(binaryContentMapper.toDto(content1))
                    .willReturn(dto1);

            given(binaryContentMapper.toDto(content2))
                    .willReturn(dto2);

            // when
            List<BinaryContentDto> result = binaryContentService.getBinaryContentsByIdIn(ids);

            // then
            assertThat(result)
                    .containsExactly(dto1, dto2);

            then(binaryContentRepository).should()
                    .findAllByIdIn(ids);

            then(binaryContentMapper).should()
                    .toDto(content1);

            then(binaryContentMapper).should()
                    .toDto(content2);
        }

        @Test
        @DisplayName("해당하는 BinaryContent가 없으면 빈 목록을 반환한다")
        void get_fail() {
            // given
            List<UUID> ids = List.of(UUID.randomUUID());

            given(binaryContentRepository.findAllByIdIn(ids))
                    .willReturn(List.of());

            // when
            List<BinaryContentDto> result = binaryContentService.getBinaryContentsByIdIn(ids);

            // then
            assertThat(result)
                    .isEmpty();

        }
    }

    @Nested
    @DisplayName("BinaryContent 삭제")
    class DeleteBinaryContent {

        @Test
        @DisplayName("BinaryContent가 존재하면 삭제한다")
        void delete_success() {
            // given
            UUID binaryContentId = UUID.randomUUID();

            BinaryContent binaryContent = mock(BinaryContent.class);

            given(binaryContentRepository.findById(binaryContentId))
                    .willReturn(Optional.of(binaryContent));

            given(binaryContent.getId())
                    .willReturn(binaryContentId);

            // when
            binaryContentService.deleteBinaryContent(binaryContentId);

            // then
            then(binaryContentRepository).should()
                    .deleteById(binaryContentId);
        }

        @Test
        @DisplayName("BinaryContent가 존재하지 않으면 예외가 발생한다")
        void delete_fail() {
            // given
            UUID binaryContentId = UUID.randomUUID();

            given(binaryContentRepository.findById(binaryContentId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> binaryContentService.deleteBinaryContent(binaryContentId))
                    .isInstanceOf(BinaryContentNotFoundException.class);

            then(binaryContentRepository).should(never())
                    .deleteById(binaryContentId);
        }
    }

}