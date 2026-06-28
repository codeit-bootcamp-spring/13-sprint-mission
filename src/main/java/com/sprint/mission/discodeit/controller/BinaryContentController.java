package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor //final이 선언된 필드를 대상으로 생성자를 자동 생성하는 Lombok 어노테이션
@Controller //해당 클래스를 spring mvc의 controller(컨트롤러)로 등록함.
@ResponseBody //모든 메서드의 반환값을 view(jsp, thymeleaf 등)가 아닌 HTTP Response body(JSON 등)로 반환하도록 설정함
@RequestMapping("/api/binaryContent")//이 컨트롤러에서 처리하는 모든 요청의 공통 URL을 지정함.
public class BinaryContentController {
    private final BinaryContentService binaryContentService; //바이너리 파일(BinaryComtent)관련 비즈니스 로직을 처리하는 서비스. (final로 선언하여 생성자를 통해 의존성 주입을 강제함.

    @RequestMapping(path = "find") //하나의 바이너리를 조회하는 요청을 처리하는 메서드
    public ResponseEntity<BinaryContent> find(@RequestParam("binaryContentId") UUID binaryContentId) {
        //요청으로 전달받은 바이너리의 UUID를 이용하여 서비스 계층에서 해당 데이터를 조회함.
        BinaryContent binaryContent = binaryContentService.find(binaryContentId);
        //HTTP 상태 코드 200(OK)와 함께 조회한 바이너리 객체를 리스포스 body에 담아 반환함.
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(binaryContent);
    }

    @RequestMapping(path = "findAllByIdIn") //여러개의 바이너리를 한번에 조회하는 요청을 처리하는 메서드
    public ResponseEntity<List<BinaryContent>> findAllByIdIn(
            @RequestParam("binaryContentIds") List<UUID> binaryContentIds) { //요청 파라미터(binaryContentIds)로 전달된 여러 개의 UUID를 List<UUID>형테로 전달받음.
        List<BinaryContent> binaryContents = binaryContentService.findAllByIdIn(binaryContentIds); //전달받은 UUID 목록을 이용하여 서비스계층에서 여러개의 바이너리를 조회함.
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(binaryContents);
    }
}
