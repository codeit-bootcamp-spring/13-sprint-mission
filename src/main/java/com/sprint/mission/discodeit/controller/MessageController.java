package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.file.FileReadStatusRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor //final이 선언된 필드를 대상으로 생성자를 자동 생성하는 Lombok 어노테이션
@Controller //해당 클래스를 spring mvc의 controller(컨트롤러)로 등록함.
@ResponseBody //모든 메서드의 반환값을 view(jsp, thymeleaf 등)가 아닌 HTTP Response body(JSON 등)로 반환하도록 설정함
@RequestMapping("/api/message")//이 컨트롤러에서 처리하는 모든 요청의 공통 URL을 지정함.
public class MessageController {
    private final MessageService messageService; //메서드 관련 비즈니스 로직을 처리하는 서비스 객체
    private final FileReadStatusRepository fileReadStatusRepository; //메시지 읽을 상태를 관린하는 레포지토리

    @RequestMapping( //메시지를 생성하는 요청을 처리하는 메서드
            path = "create",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Message> create(
    @RequestPart("messageCreateRequest") MessageCreateRequest messageCreateRequest, //메시지 정보(JSON)를 전달 받음.
    @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments //첨부파일 목록을 전달받음.
    ) {
      List<BinaryContentCreateRequest> attachmentRequests = Optional.ofNullable(attachments) //전달 받은 MultiparFile 목록을 BinaryContentCreateRequest 목록으로 변환함.
              .map(files -> files.stream() //첨부파일이 존재하는 경우 status으로 변환 작업을 수행함.
                      .map(file -> { //MultipartFile 하나를 BinaryContentCreateRequest 하나로 변환함.
                          try {
                              return new BinaryContentCreateRequest( //파일명, MIME 타입, 파일 데이터를 이용하여 BinaryContentCreateRequest 객체를 생성함.
                                      file.getOriginalFilename(),
                                      file.getContentType(),
                                      file.getBytes()
                              );
                          } catch (IOException e) { //파일 읽기 중 IOException이 발생하면 RuntimeException으로 변환하여 예외를 전달함.
                              throw new RuntimeException(e);
                          }
                      })
                      .toList()) //Stream 결과를 List로 변환함.
              .orElse(new ArrayList<>()); //첨부파일이 없는 경우 빈 List를 생성함.
      Message createdMessage = messageService.create(messageCreateRequest, attachmentRequests); //메시지 정보와 첨부파일 정보를 서비스 계층으로 전달하여 새로운 메시지를 생성함.
      return ResponseEntity
              .status(HttpStatus.CREATED)
              .body(createdMessage);
    }

    @RequestMapping(path = "update") //메시지를 수정하는 요청을 처리하는 메서드
    public ResponseEntity<Message> update(
            @RequestParam("messageId") UUID messageId, //수정할 메시지의 UUID를 요청 파라미터로 전달받음.
            @RequestBody MessageUpdateRequest request) { //수정할 메시지 정보를 HTTP Body로 전달받음.
        Message updateMessage = messageService.update(messageId, request); //서비스 계층에서 메시지를 수정함.
        return  ResponseEntity
                .status(HttpStatus.OK)
                .body(updateMessage);
    }

    @RequestMapping(path = "delete") //메시지를 삭제하는 요청을 처리하는 메서드
    public ResponseEntity<Void> delete(
            @RequestParam("messageId") UUID messageId) {
        messageService.delete(messageId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @RequestMapping(path = "findAllByChannelId") //특정 채널의 모든 메시지를 조회하는 요청을 처리하는 메서드
    public ResponseEntity<List<Message>> findAllByChannelId(
            @RequestParam("channelId") UUID channelId) {
        List<Message> messages = messageService.findAllByChannelId(channelId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(messages);
    }

}
