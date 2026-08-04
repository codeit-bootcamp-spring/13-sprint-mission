package com.sprint.mission.discodeit.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

// 메시지 생성 요청 DTO
// 메시지를 생성할 때 필요한 데이터를 담는 클래스
@Getter
@NoArgsConstructor
public class MessageCreateRequest {

    // 메시지 내용
    // 수정됨: 프론트가 body, text, message 등으로 보낼 가능성 대비
    @NotBlank(message = "메시지 내용은 필수입니다.")
    @Size(
            max = 1000,
            message = "메시지 내용은 1000자 이하여야 합니다."
    )
    @JsonAlias({"body", "text", "message"})
    private String content;

    // 메시지를 작성한 사용자 id
    // 수정됨: 프론트가 userId, senderId, writerId 등으로 보낼 가능성 대비
    @NotNull(message = "작성자 ID는 필수입니다.")
    @JsonAlias({"userId", "senderId", "writerId"})
    private UUID authorId;

    // 메시지가 작성될 채널 id
    // 수정됨: 프론트가 roomId로 보낼 가능성 대비
    @NotNull(message = "채널 ID는 필수입니다.")
    @JsonAlias({"roomId"})
    private UUID channelId;

    // 첨부파일 생성 요청 목록
    // 첨부파일이 없으면 null 또는 빈 리스트일 수 있음
    // 수정됨: 프론트가 attachmentFiles, files 등으로 보낼 가능성 대비
    @Valid
    @JsonAlias({"attachmentFiles", "files"})
    private List<BinaryContentCreateRequest> attachments;

    // 메시지 생성 요청 객체를 생성하는 생성자
    // 첨부파일이 있는 메시지를 생성할 때 사용
    public MessageCreateRequest(
            String content,
            UUID authorId,
            UUID channelId,
            List<BinaryContentCreateRequest> attachments
    ) {
        this.content = content;
        this.authorId = authorId;
        this.channelId = channelId;
        this.attachments = attachments;
    }

    // 기존 테스트 코드 호환용 생성자
    // 첨부파일 없이 메시지를 생성할 때 사용
    public MessageCreateRequest(String content, UUID authorId, UUID channelId) {
        this(content, authorId, channelId, null);
    }
}