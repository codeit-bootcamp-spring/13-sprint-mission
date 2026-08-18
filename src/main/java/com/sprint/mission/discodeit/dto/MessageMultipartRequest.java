package com.sprint.mission.discodeit.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MessageMultipartRequest {

    /*
     * 메시지 내용 필드 호환
     */
    private String content;
    private String body;
    private String text;
    private String message;

    /*
     * 작성자 ID 필드 호환
     *
     * UUID로 바로 받으면 잘못된 UUID가 들어왔을 때
     * Spring 바인딩 단계에서 오류가 발생할 수 있으므로
     * 문자열로 받은 뒤 Mapper에서 변환한다.
     */
    private String authorId;
    private String userId;
    private String senderId;

    /*
     * 채널 ID 필드 호환
     */
    private String channelId;
    private String roomId;

    /*
     * 프론트에서 messageCreateRequest를
     * application/json Blob 형태의 multipart 파트로 전달하므로
     * MultipartFile로 받는다.
     */
    private MultipartFile messageCreateRequest;

    /*
     * 기존 문자열 기반 요청 이름 호환
     */
    private String request;
    private String messageRequest;

    /*
     * 첨부파일 파트 이름 호환
     */
    private List<MultipartFile> attachments;
    private List<MultipartFile> files;
}