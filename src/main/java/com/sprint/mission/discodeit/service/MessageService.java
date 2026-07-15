package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageResponse;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Message 관련 기능을 정의하는 Service 인터페이스
// 메시지 생성, 조회, 수정, 삭제 기능의 규칙을 선언함
public interface MessageService {

    // 메시지 생성 기능
    // MessageCreateRequest DTO를 받아 메시지를 생성함
    // 첨부파일이 있는 경우 request.attachments도 함께 처리함
    MessageResponse create(MessageCreateRequest request);

    // id로 메시지 단건 조회 기능
    // Message 엔티티가 아니라 MessageResponse DTO를 반환함
    MessageResponse read(UUID id);

    // 특정 Channel에 작성된 메시지 목록 조회 기능
    // 기존 프론트/기존 코드 호환용으로 유지
    List<MessageResponse> findAllByChannelId(UUID channelId);

    /*
     * 커서 페이지네이션 기반 메시지 목록 조회
     *
     * cursor가 null이면 최신 메시지부터 조회
     * cursor가 있으면 해당 createdAt보다 오래된 메시지를 조회
     * size는 기본적으로 50개 사용
     */
    PageResponse<MessageResponse> findAllByChannelId(
            UUID channelId,
            Instant cursor,
            int size
    );

    // 메시지 수정 기능
    // MessageUpdateRequest DTO를 받아 메시지 내용을 수정함
    MessageResponse update(MessageUpdateRequest request);

    // 메시지 삭제 기능
    // 메시지 삭제 시 첨부파일도 함께 삭제함
    void delete(UUID id);
}