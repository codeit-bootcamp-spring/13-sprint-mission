package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController // 모든 메서드 반환값 자동 JSON
@RequiredArgsConstructor
@RequestMapping("/api/channels")
@Tag(name = "Channel", description = "Channel API") // 그룹 묶기
public class ChannelController { // 채널 관리

  private final ChannelService channelService;

  // 공개 채널 생성
  @Operation(summary = "Public Channel 생성") // 엔드포인트 설명
  @ApiResponse(responseCode = "201", description = "Public Channel이 성공적으로 생성됨")
  @RequestMapping(path = "/public", method = RequestMethod.POST)
  public ResponseEntity<ChannelDto> createPublicChannel(
      @Valid @RequestBody PublicChannelCreateRequest request) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(channelService.createPublicChannel(request));
  }

  // 비공개 채널 생성
  @Operation(summary = "Private Channel 생성")
  @ApiResponse(responseCode = "201", description = "Private Channel이 성공적으로 생성됨")
  @RequestMapping(path = "/private", method = RequestMethod.POST)
  public ResponseEntity<ChannelDto> createPrivateChannel(
      @Valid @RequestBody PrivateChannelCreateRequest request) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(channelService.createPrivateChannel(request));
  }

  // 공개 채널 정보 수정
  @Operation(summary = "Channel 정보 수정")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Channel 정보가 성공적으로 수정됨"),
      @ApiResponse(responseCode = "404", description = "Channel을 찾을 수 없음"),
      @ApiResponse(responseCode = "400", description = "Private Channel은 수정할 수 없음")
  })
  @RequestMapping(path = "/{channelId}", method = RequestMethod.PATCH)
  public ResponseEntity<ChannelDto> updatePublicChannel(
      @Parameter(name = "channelId", in = ParameterIn.PATH, description = "수정할 Channel ID", required = true,
          schema = @Schema(type = "string", format = "uuid"))
      @PathVariable UUID channelId,
      @Valid @RequestBody PublicChannelUpdateRequest request) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(channelService.update(channelId, request)); // 변수명을 쓰지 않고 바로 return
  }

  // 채널 삭제
  @Operation(summary = "Channel 삭제")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Channel이 성공적으로 삭제됨"),
      @ApiResponse(responseCode = "404", description = "Channel을 찾을 수 없음")
  })
  @RequestMapping(path = "/{channelId}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> deleteChannel(
      @Parameter(name = "channelId", in = ParameterIn.PATH, description = "삭제할 Channel ID", required = true,
          schema = @Schema(type = "string", format = "uuid"))
      @PathVariable UUID channelId) {
    channelService.delete(channelId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  // 특정 사용자가 볼 수 있는 채널목록 조회
  @Operation(summary = "User가 참여 중인 Channel 목록 조회")
  @ApiResponse(
      responseCode = "200",
      description = "Channel 목록 조회 성공",
      content = @Content(
          array = @ArraySchema(
              schema = @Schema(implementation = ChannelDto.class))))
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<ChannelDto>> findAllByUserId(
      @Parameter(name = "userId", in = ParameterIn.QUERY, description = "조회할 User ID", required = true,
          schema = @Schema(type = "string", format = "uuid"))
      @RequestParam("userId") UUID userId) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(channelService.findAllByUserId(userId));
  }
}
