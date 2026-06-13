package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class Channel implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id; // [요구사항] id는 생성자에서 초기화 // [요구사항] 내부에서 초기화
    private String channelTitles; // 이름은 밖에서 받아옴
    private final Instant createdAt; // [요구사항] createdAt은 생성자에서 초기화 // [요구사항] 내부에서 초기화
    private Instant updatedAt; // 처음 생성 시엔 수정 시간도 생성 시간과 같음

    public Channel(String channelTitles) {
        this.id = UUID.randomUUID();
        this.channelTitles = channelTitles;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void updateTitles(Channel channel) {
        this.channelTitles = channel.getChannelTitles();
        this.updatedAt = Instant.now();
    }

}

/*
[ ] 등록 -> 채널(대화창) 생성
[ ] 조회(단건, 다건) -> 채널 검색(특정 채널 검색/전체 채널 조회)
[ ] 수정 -> 채널 이름 수정
[ ] 수정된 데이터 조회 -> 채널 이름 재검색
[ ] 삭제 -> 채널 삭제
[ ] 조회를 통해 삭제되었는지 확인 -> 채널 이름 재검색
 */