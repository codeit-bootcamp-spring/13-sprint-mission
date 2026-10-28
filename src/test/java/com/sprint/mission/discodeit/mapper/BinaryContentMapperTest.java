package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.DownloadDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.storage.ResourceDownloadResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 MapStruct 매퍼로 파일 메타데이터와 각 업로드 상태가 DTO에 반영되는지 확인한다.
 * 서비스 단위 테스트에서 Mock Mapper가 생략한 실제 필드 매핑을 이 테스트가 담당한다.
 */
@DisplayName("바이너리 파일 매퍼 단위 테스트")
class BinaryContentMapperTest {
    private final BinaryContentMapper mapper = Mappers.getMapper(BinaryContentMapper.class);

    @ParameterizedTest
    @EnumSource(BinaryContentStatus.class)
    @DisplayName("파일 정보와 업로드 상태를 DTO로 매핑한다")
    void toDto_mapsMetadata(BinaryContentStatus status) {
        UUID id = UUID.randomUUID();
        BinaryContent content = BinaryContent.createProcessing("image.png", "image/png", 42L);
        ReflectionTestUtils.setField(content, "id", id);
        content.updateStatus(status);

        assertThat(mapper.toDto(content)).isEqualTo(new BinaryContentDto(id, "image.png", 42L, "image/png", status));
    }

    @Test
    @DisplayName("다운로드 응답은 매핑한 메타데이터와 저장소의 다운로드 결과를 포함한다")
    void toDownloadDto_preservesDownloadResult() {
        BinaryContent content = BinaryContent.createProcessing("file.txt", "text/plain", 1L);
        ResourceDownloadResult result = new ResourceDownloadResult(new ByteArrayResource(new byte[]{1}));

        DownloadDto dto = mapper.toDownloadDto(content, result);

        assertThat(dto.binaryContent()).isEqualTo(new BinaryContentDto(null, "file.txt", 1L, "text/plain", BinaryContentStatus.PROCESSING));
        assertThat(dto.result()).isSameAs(result);
    }
}
