package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

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

  @Test
  @DisplayName("파일 정보를 조회하면 DTO를 반환한다")
  void find_returnsBinaryContentDto() {
    // given
    UUID id = UUID.randomUUID();
    BinaryContent binaryContent = mock(BinaryContent.class);
    BinaryContentDto binaryContentDto = mock(BinaryContentDto.class);

    when(binaryContentRepository.findById(id))
        .thenReturn(Optional.of(binaryContent));
    when(binaryContentMapper.toDto(binaryContent))
        .thenReturn(binaryContentDto);

    // when
    BinaryContentDto result = binaryContentService.find(id);

    // then
    assertThat(result).isEqualTo(binaryContentDto);

    verify(binaryContentRepository).findById(id);
    verify(binaryContentMapper).toDto(binaryContent);
  }

  @Test
  @DisplayName("존재하지 않는 파일을 조회하면 예외가 발생한다")
  void find_throwsException_whenBinaryContentDoesNotExist() {
    // given
    UUID id = UUID.randomUUID();

    when(binaryContentRepository.findById(id))
        .thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> binaryContentService.find(id))
        .isInstanceOf(BinaryContentNotFoundException.class);

    verify(binaryContentRepository).findById(id);
  }

  @Test
  @DisplayName("파일 ID 목록으로 파일 정보 목록을 조회한다")
  void findByIdIn_returnsBinaryContentDtos() {
    // given
    UUID firstId = UUID.randomUUID();
    UUID secondId = UUID.randomUUID();
    List<UUID> ids = List.of(firstId, secondId);

    BinaryContent firstBinaryContent = mock(BinaryContent.class);
    BinaryContent secondBinaryContent = mock(BinaryContent.class);

    BinaryContentDto firstDto = mock(BinaryContentDto.class);
    BinaryContentDto secondDto = mock(BinaryContentDto.class);

    when(binaryContentRepository.findAllById(ids))
        .thenReturn(List.of(firstBinaryContent, secondBinaryContent));
    when(binaryContentMapper.toDto(firstBinaryContent))
        .thenReturn(firstDto);
    when(binaryContentMapper.toDto(secondBinaryContent))
        .thenReturn(secondDto);

    // when
    List<BinaryContentDto> result = binaryContentService.findByIdIn(ids);

    // then
    assertThat(result)
        .containsExactly(firstDto, secondDto);

    verify(binaryContentRepository).findAllById(ids);
    verify(binaryContentMapper).toDto(firstBinaryContent);
    verify(binaryContentMapper).toDto(secondBinaryContent);
  }

  @Test
  @DisplayName("파일을 다운로드하면 스토리지의 응답을 반환한다")
  void download_returnsStorageResponse() {
    // given
    UUID id = UUID.randomUUID();
    BinaryContent binaryContent = mock(BinaryContent.class);
    BinaryContentDto binaryContentDto = mock(BinaryContentDto.class);

    ResponseEntity<Void> expectedResponse =
        ResponseEntity.ok().build();

    when(binaryContentRepository.findById(id))
        .thenReturn(Optional.of(binaryContent));
    when(binaryContentMapper.toDto(binaryContent))
        .thenReturn(binaryContentDto);

    doReturn(expectedResponse)
        .when(binaryContentStorage)
        .download(binaryContentDto);

    // when
    ResponseEntity<?> result = binaryContentService.download(id);

    // then
    assertThat(result).isEqualTo(expectedResponse);

    verify(binaryContentRepository).findById(id);
    verify(binaryContentMapper).toDto(binaryContent);
    verify(binaryContentStorage).download(binaryContentDto);
  }
}