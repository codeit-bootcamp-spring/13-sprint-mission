package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/readStatuses", "/api/read-statuses"})
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    /*
     * API 명세 v1.2 기준
     * POST /api/readStatuses
     *
     * 기존 호환:
     * POST /api/read-statuses
     */
    @PostMapping
    public ResponseEntity<ReadStatusResponse> create(
            @Valid @RequestBody ReadStatusCreateRequest request
    ) {
        ReadStatusResponse response = readStatusService.create(request);

        URI location = URI.create("/api/readStatuses/" + response.getId());

        return ResponseEntity
                .created(location)
                .body(response);
    }

    /*
     * 기존 호환용 단건 조회
     *
     * API 명세 v1.2에는 목록 조회 중심이지만,
     * 기존 테스트/Postman 호환을 위해 유지
     */
    @GetMapping("/{readStatusId}")
    public ResponseEntity<ReadStatusResponse> find(
            @PathVariable UUID readStatusId
    ) {
        ReadStatusResponse response = readStatusService.find(readStatusId);

        return ResponseEntity.ok(response);
    }

    /*
     * API 명세 v1.2 기준
     * GET /api/readStatuses?userId=...
     *
     * 기존 호환:
     * GET /api/read-statuses?userId=...
     */
    @GetMapping
    public ResponseEntity<List<ReadStatusResponse>> findAllByUserId(
            @RequestParam UUID userId
    ) {
        List<ReadStatusResponse> responses =
                readStatusService.findAllByUserId(userId);

        return ResponseEntity.ok(responses);
    }

    /*
     * 기존 Postman/Swagger 테스트 호환용
     *
     * PATCH /api/readStatuses
     * PATCH /api/read-statuses
     *
     * body에 id가 들어오는 경우 처리
     */
    @PatchMapping
    public ResponseEntity<ReadStatusResponse> update(
            @Valid @RequestBody ReadStatusUpdateRequest request
    ) {
        ReadStatusResponse response = readStatusService.update(request);

        return ResponseEntity.ok(response);
    }

    /*
     * API 명세 v1.2 기준
     * PATCH /api/readStatuses/{readStatusId}
     *
     * body:
     * {
     *   "newLastReadAt": "2026-07-10T01:30:55.469015Z"
     * }
     *
     * 기존 호환:
     * PATCH /api/read-statuses/{readStatusId}
     */
    @PatchMapping("/{readStatusId}")
    public ResponseEntity<ReadStatusResponse> updateByPathVariable(
            @PathVariable UUID readStatusId,
            @Valid @RequestBody ReadStatusUpdateRequest request
    ) {
        ReadStatusUpdateRequest fixedRequest = new ReadStatusUpdateRequest(
                readStatusId,
                request.getLastReadAt()
        );

        ReadStatusResponse response = readStatusService.update(fixedRequest);

        return ResponseEntity.ok(response);
    }

    /*
     * 기존 호환용 삭제
     *
     * DELETE /api/readStatuses/{readStatusId}
     * DELETE /api/read-statuses/{readStatusId}
     */
    @DeleteMapping("/{readStatusId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID readStatusId
    ) {
        readStatusService.delete(readStatusId);

        return ResponseEntity
                .noContent()
                .build();
    }
}