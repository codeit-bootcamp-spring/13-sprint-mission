package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor //final이 선언된 필드를 대상으로 생성자를 자동 생성하는 Lombok 어노테이션
@RestController
@RequestMapping("/api/channels")//이 컨트롤러에서 처리하는 모든 요청의 공통 URL을 지정함.
public class ChannelController {

  private final ChannelService channelService; //채널과 관련된 비즈니스 로직을 처리하는 서비스 객체

  @PostMapping("/public") //공개 채널(Public Channel)을 생성하는 요청을 처리하는 메서드
  public ResponseEntity<Channel> create(@RequestBody PublicChannelCreateRequest request) {
    Channel createChannel = channelService.create(
        request); //클라이언트가 전달한 공개 채넣 생성 정보를 서비스 계층으로 전달하여 새로운 채널을 생성함.
    //생성이 완료되었으므로 HTTP 상태코드 201(CREATED)과 함께 생성된 channel 정보를 응담으로 반환함.
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createChannel);
  }

  @PostMapping("private") //비공개 채널(Private Channel)을 생성하는 요청을 처리하는 메서드
  public ResponseEntity<Channel> create(@RequestBody PrivateChannelCreateRequest request) {
    Channel createChannel = channelService.create(
        request); //클라이언트가 전달한 비공개 채널 생성 정보를 이용하여 서비스 계층에게 새로운 비공개 채널을 생성함.
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createChannel);
  }

  @PatchMapping("/{channelId}") //공개채널 정보를 수정하는 요청을 처리하는 메서드
  public ResponseEntity<Channel> update(
      @PathVariable UUID channelId, //수정할 채널의 고유 식별자(UUID)를 요청 파라미터로 전달받음.
      @RequestBody PublicChannelUpdateRequest request) { //수정할 채널 정보를 HTTP Body에서 전달받음.
    Channel updateChannel = channelService.update(channelId,
        request); //전달 받은 채널 ID와 수정정보를 이용하여 서비스 계층에서 채널 정보를 수정함.
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(updateChannel);
  }

  @DeleteMapping("/{channelId}") //채널을 삭제하는 요청을 처리하는 메서드
  public ResponseEntity<Void> delete(
      @PathVariable UUID channelId) { //삭제항 채널의 UUID를 요청 파라미터로 전달받음
    channelService.delete(channelId); //서비스 계층에서 해당 채널을 삭제함.
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

  @GetMapping //특정 사용자가 참여하고 있는 모든 채널을 조회하는 요청을 처리하는 메서드
  public ResponseEntity<List<ChannelDto>> findAll(
      @RequestParam UUID userId) { //조회할 사용자의 UUID를 요청 파라미터로 전달받음.
    List<ChannelDto> channels = channelService.findAllByUserId(
        userId); //전달받은 사용자 ID를 이용하여 서비스 계층에서 사용자가 속한 모든 채널 목록을 조회함.
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(channels);
  }
}
