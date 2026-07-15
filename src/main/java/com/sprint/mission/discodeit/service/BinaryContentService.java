package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentDownloadResponse;
import com.sprint.mission.discodeit.dto.BinaryContentResponse;

import java.util.List;
import java.util.UUID;

// BinaryContent 관련 기능을 정의하는 Service 인터페이스
// BinaryContent는 프로필 이미지, 메시지 첨부파일 같은 바이너리 파일의 메타데이터를 관리함
public interface BinaryContentService {

    /*
     * BinaryContent 생성 기능
     *
     * DB에는 fileName, contentType, size 같은 메타데이터만 저장하고,
     * 실제 byte[] 데이터는 BinaryContentStorage에 저장한다.
     */
    BinaryContentResponse create(BinaryContentCreateRequest request);

    /*
     * id로 BinaryContent 단건 메타데이터 조회
     *
     * 실제 bytes 데이터는 포함하지 않음
     */
    BinaryContentResponse find(UUID id);

    /*
     * id로 BinaryContent 실제 파일 다운로드용 조회
     *
     * DB의 메타데이터와 Storage의 byte[] 데이터를 함께 사용해서
     * 다운로드 응답 DTO를 만든다.
     */
    BinaryContentDownloadResponse findForDownload(UUID id);

    /*
     * 여러 id에 해당하는 BinaryContent 목록 조회
     *
     * 메시지의 attachmentIds를 실제 첨부파일 응답 목록으로 변환할 때 사용.
     * 실제 bytes 데이터는 포함하지 않음.
     */
    List<BinaryContentResponse> findAllByIdIn(List<UUID> ids);

    /*
     * id로 BinaryContent 삭제
     *
     * DB 메타데이터와 Storage의 실제 파일을 함께 삭제한다.
     */
    void delete(UUID id);
}