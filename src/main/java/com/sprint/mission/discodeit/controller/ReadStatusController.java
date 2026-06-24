package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@Controller
@ResponseBody
@RequestMapping("/readstatus")
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    // 메시지 수신 정보 생성
    @RequestMapping(method = RequestMethod.POST)
    public Object createReadStatus(
            @RequestBody ReadStatusCreateRequest request
    ) {

        return readStatusService.create(request);
    }

    // 특정 사용자의 메시지 수신 정보 조회
    @RequestMapping(
            value = "/user/{userId}",
            method = RequestMethod.GET
    )
    public Object getReadStatusByUser(
            @PathVariable UUID userId
    ) {

        return readStatusService.findAllByUserId(userId);
    }

    // 메시지 수신 정보 수정
    @RequestMapping(
            value = "/{readStatusId}",
            method = RequestMethod.PUT
    )
    public Object updateReadStatus(
            @PathVariable UUID readStatusId,
            @RequestBody ReadStatusUpdateRequest request
    ) {

        return readStatusService.update(
                readStatusId,
                request
        );
    }
}