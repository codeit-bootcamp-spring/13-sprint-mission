package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor //final이 선언된 필드를 대상으로 생성자를 자동 생성하는 Lombok 어노테이션
@Controller //해당 클래스를 spring mvc의 controller(컨트롤러)로 등록함.
@ResponseBody //모든 메서드의 반환값을 view(jsp, thymeleaf 등)가 아닌 HTTP Response body(JSON 등)로 반환하도록 설정함
@RequestMapping("/api/ReadStatus")//이 컨트롤러에서 처리하는 모든 요청의 공통 URL을 지정함.
public class ReadStatusController {
    private final ReadStatusService readStatusService; //읽음 상태와 관련된 비즈니스 로직을 처리하는 서비스 객체

    @RequestMapping(path = "create") //새로운 읽음 상태를 생성하는 요청을 처리하는 메서드
    public ResponseEntity<ReadStatus> create(@RequestPart ReadStatusCreateRequest request) {
        ReadStatus createReadStatus = readStatusService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createReadStatus);
    }

    @RequestMapping(path = "update") //기존 읽음상태를 수정하는 요청을 처리하는 메서드
    public ResponseEntity<ReadStatus> update(
            @RequestParam("readStatusId")UUID readStatusId,
            @RequestBody ReadStatusUpdateRequest request){
        ReadStatus updateReadStatus = readStatusService.update(readStatusId, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updateReadStatus);
    }

    @RequestMapping(path = "findAllByUserId") //특정 사용자의 모든 읽음 상태를 조회하는 요청을 처리하는 메서드
    public ResponseEntity<List<ReadStatus>> findAllByUserId(@RequestParam("userId") UUID userId){
        List<ReadStatus> readStatuses = readStatusService.findAllByUserId(userId);
        return  ResponseEntity
                .status(HttpStatus.OK)
                .body(readStatuses);
    }
}
