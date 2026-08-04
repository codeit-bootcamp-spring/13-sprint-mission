package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Channel", description = "Channel API")
@RequestMapping("/api/channels")
public interface ChannelApi {

  //공식 채널 생성
  @Operation(summary = "Public Channel 생성")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Public Channel이 성공적으로 생성됨"),
  })
  @PostMapping("/public")
  ResponseEntity<ChannelDto> create(@RequestBody PublicChannelCreateRequest request);

  //비공식 채널 생성
  @Operation(summary = "Private Channel 생성")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Private Channel이 성공적으로 생성됨")
  })
  @PostMapping("/private")
  ResponseEntity<ChannelDto> create(@RequestBody PrivateChannelCreateRequest request);

  //수정
  @Operation(summary = "Channel 정보 수정")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Channel 정보가 성공적으로 수정됨"),
      @ApiResponse(responseCode = "400", description = "Private Channel은 수정할 수 없음",
          content = @Content(examples = @ExampleObject(value = "Channel with id {channel} not found"))),
      @ApiResponse(responseCode = "404", description = "Channel을 찾을 수 없음",
          content = @Content(examples = @ExampleObject(value = "Private channel cannot be updated")))
  })
  @PatchMapping("/{channelId}")
  ResponseEntity<ChannelDto> update(
      @Parameter(description = "수정할 channel ID") @PathVariable UUID channelId,
      @Parameter(description = "수정할 channel 정보") @RequestBody PublicChannelUpdateRequest request
  );

  //삭제
  @Operation(summary = "Channel 삭제")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Channel이 성공적으로 삭제됨"),
      @ApiResponse(responseCode = "404", description = "Channel을 찾을 수 없음",
          content = @Content(examples = @ExampleObject(value = "Channel with id {channelId} not found")))
  })
  @DeleteMapping("/{channelId}")
  ResponseEntity<Void> delete(
      @Parameter(description = "삭제할 Channel ID") @PathVariable UUID channelId);

  //모든 채널 조회
  @Operation(summary = "User가 참여 중인 Channel 목록 조회")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "Channel 목록 조회 성공")})
  @GetMapping
  ResponseEntity<List<ChannelDto>> findAll(
      @Parameter(description = "조회할 User ID") @RequestParam UUID userId);
}
