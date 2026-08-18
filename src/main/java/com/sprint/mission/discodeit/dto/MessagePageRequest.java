package com.sprint.mission.discodeit.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class MessagePageRequest {

    @NotNull(message = "채널 ID는 필수입니다.")
    private UUID channelId;

    /*
     * API 명세 기준 커서
     */
    private Instant cursor;

    /*
     * 기존 요청 호환용 커서
     */
    private Instant after;

    @Min(
            value = 1,
            message = "페이지 크기는 1 이상이어야 합니다."
    )
    @Max(
            value = 100,
            message = "페이지 크기는 100 이하여야 합니다."
    )
    private int size = 50;

    /*
     * 기존 프론트 요청 호환용
     * 커서 페이지네이션에서는 사용하지 않는다.
     */
    private Integer page;

    private String sort;

    public Instant getActualCursor() {
        return cursor != null ? cursor : after;
    }
}