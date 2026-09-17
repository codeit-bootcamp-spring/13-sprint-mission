package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.DownloadDto;
import com.sprint.mission.discodeit.service.basic.BinaryContentService;
import com.sprint.mission.discodeit.storage.ResourceDownloadResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BinaryContentController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("BinaryContentController 슬라이스 테스트")
class BinaryContentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    BinaryContentService binaryContentService;

    @Test
    @DisplayName("파일 목록 조회 성공 - binaryContentIds query parameter가 있으면 200 OK와 파일 목록 반환")
    void listByIds_returnsOkAndBinaryContents_whenIdsAreProvided() throws Exception {
        // given
        // BinaryContentController.listByIds(...)는 request body 없이 binaryContentIds query parameter 목록을 받는다.
        // 같은 이름의 query parameter를 여러 번 전달하면 List<UUID>로 바인딩되어야 한다.
        UUID firstBinaryContentId = UUID.randomUUID();
        UUID secondBinaryContentId = UUID.randomUUID();
        BinaryContentDto firstBinaryContent = new BinaryContentDto(
                firstBinaryContentId,
                "first-file.png",
                7L,
                MediaType.IMAGE_PNG_VALUE
        );
        BinaryContentDto secondBinaryContent = new BinaryContentDto(
                secondBinaryContentId,
                "second-file.txt",
                11L,
                MediaType.TEXT_PLAIN_VALUE
        );

        // query parameter에서 바인딩된 UUID 목록은 아래 captor로 검증할 것이므로
        // stub은 타입 기준으로 열어 둔다.
        given(binaryContentService.findAllByIdIn(any())).willReturn(List.of(firstBinaryContent, secondBinaryContent));

        // when
        // GET /api/binaryContents?binaryContentIds={id1}&binaryContentIds={id2} 요청을 전송한다.
        mockMvc.perform(get("/api/binaryContents")
                        .param("binaryContentIds", firstBinaryContentId.toString(), secondBinaryContentId.toString())
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // 응답 상태가 200 OK이고, Service가 반환한 BinaryContentDto 목록이 JSON 배열로 직렬화되는지 확인한다.
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(firstBinaryContentId.toString()))
                .andExpect(jsonPath("$[0].fileName").value(firstBinaryContent.fileName()))
                .andExpect(jsonPath("$[0].size").value(firstBinaryContent.size()))
                .andExpect(jsonPath("$[0].contentType").value(firstBinaryContent.contentType()))
                .andExpect(jsonPath("$[1].id").value(secondBinaryContentId.toString()))
                .andExpect(jsonPath("$[1].fileName").value(secondBinaryContent.fileName()))
                .andExpect(jsonPath("$[1].size").value(secondBinaryContent.size()))
                .andExpect(jsonPath("$[1].contentType").value(secondBinaryContent.contentType()));

        // query parameter 문자열들이 UUID 목록으로 변환되어 Service에 전달됐는지 확인한다.
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UUID>> idsCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(binaryContentService).findAllByIdIn(idsCaptor.capture());

        assertThat(idsCaptor.getValue()).containsExactly(firstBinaryContentId, secondBinaryContentId);
    }

    @Test
    @DisplayName("파일 단건 조회 성공 - binaryContentId path variable이 있으면 200 OK와 파일 정보 반환")
    void findById_returnsOkAndBinaryContent_whenIdExists() throws Exception {
        // given
        // path variable로 사용할 binaryContentId와 Service가 반환할 BinaryContentDto를 준비한다.
        UUID binaryContentId = UUID.randomUUID();
        BinaryContentDto response = new BinaryContentDto(
                binaryContentId,
                "profile.png",
                1024L,
                MediaType.IMAGE_PNG_VALUE
        );

        given(binaryContentService.findById(any(UUID.class))).willReturn(response);

        // when
        // GET /api/binaryContents/{binaryContentId} 요청을 전송한다.
        mockMvc.perform(get("/api/binaryContents/{binaryContentId}", binaryContentId)
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // 응답 상태가 200 OK이고, Service가 반환한 BinaryContentDto가 JSON으로 직렬화되는지 확인한다.
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(binaryContentId.toString()))
                .andExpect(jsonPath("$.fileName").value(response.fileName()))
                .andExpect(jsonPath("$.size").value(response.size()))
                .andExpect(jsonPath("$.contentType").value(response.contentType()));

        // path variable이 UUID로 바인딩된 뒤 Service에 전달됐는지 확인한다.
        ArgumentCaptor<UUID> binaryContentIdCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(binaryContentService).findById(binaryContentIdCaptor.capture());

        assertThat(binaryContentIdCaptor.getValue()).isEqualTo(binaryContentId);
    }

    @Test
    @DisplayName("파일 다운로드 성공 - contentType이 있으면 해당 MediaType과 attachment header 반환")
    void download_returnsResourceWithContentHeaders_whenContentTypeExists() throws Exception {
        // given
        // 다운로드 endpoint는 Service가 반환한 DownloadDto의 메타데이터로 응답 header를 만들고,
        // Resource를 응답 body로 내려준다.
        UUID binaryContentId = UUID.randomUUID();
        byte[] bytes = "download-content".getBytes(StandardCharsets.UTF_8);
        BinaryContentDto binaryContent = new BinaryContentDto(
                binaryContentId,
                "download.txt",
                (long) bytes.length,
                MediaType.TEXT_PLAIN_VALUE
        );
        DownloadDto download = new DownloadDto(binaryContent, new ResourceDownloadResult(new ByteArrayResource(bytes)));

        given(binaryContentService.download(any(UUID.class))).willReturn(download);

        // when
        // GET /api/binaryContents/{binaryContentId}/download 요청을 전송한다.
        mockMvc.perform(get("/api/binaryContents/{binaryContentId}/download", binaryContentId))

                // then
                // contentType이 있으면 해당 MediaType을 Content-Type으로 사용해야 한다.
                // 파일명은 attachment Content-Disposition header에 포함되어야 하고, body는 Resource 내용과 같아야 한다.
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, bytes.length))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString(binaryContent.fileName())))
                .andExpect(content().bytes(bytes));

        // path variable이 UUID로 바인딩된 뒤 Service에 전달됐는지 확인한다.
        ArgumentCaptor<UUID> binaryContentIdCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(binaryContentService).download(binaryContentIdCaptor.capture());

        assertThat(binaryContentIdCaptor.getValue()).isEqualTo(binaryContentId);
    }

    @Test
    @DisplayName("파일 다운로드 성공 - contentType이 null이면 application/octet-stream 반환")
    void download_returnsOctetStream_whenContentTypeIsNull() throws Exception {
        // given
        // BinaryContentDto.contentType이 null이면 Controller는 안전한 기본값인 application/octet-stream을 사용한다.
        UUID binaryContentId = UUID.randomUUID();
        byte[] bytes = "binary-content".getBytes(StandardCharsets.UTF_8);
        BinaryContentDto binaryContent = new BinaryContentDto(
                binaryContentId,
                "unknown.bin",
                (long) bytes.length,
                null
        );
        DownloadDto download = new DownloadDto(binaryContent, new ResourceDownloadResult(new ByteArrayResource(bytes)));

        given(binaryContentService.download(any(UUID.class))).willReturn(download);

        // when
        // GET /api/binaryContents/{binaryContentId}/download 요청을 전송한다.
        mockMvc.perform(get("/api/binaryContents/{binaryContentId}/download", binaryContentId))
                // then
                // contentType이 null이어도 다운로드 응답은 성공해야 한다.
                // Content-Type은 application/octet-stream이고, attachment header와 body는 유지되어야 한다.
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, bytes.length))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString(binaryContent.fileName())))
                .andExpect(content().bytes(bytes));

        // path variable이 UUID로 바인딩된 뒤 Service에 전달됐는지 확인한다.
        ArgumentCaptor<UUID> binaryContentIdCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(binaryContentService).download(binaryContentIdCaptor.capture());

        assertThat(binaryContentIdCaptor.getValue()).isEqualTo(binaryContentId);
    }

}
