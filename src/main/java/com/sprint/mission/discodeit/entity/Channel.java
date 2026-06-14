package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Channel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id; // [요구사항] id는 생성자에서 초기화 // [요구사항] 내부에서 초기화
    private String channelTitles; // 이름은 밖에서 받아옴
    private final Instant createdAt; // [요구사항] createdAt은 생성자에서 초기화 // [요구사항] 내부에서 초기화
    private Instant updatedAt; // 처음 생성 시엔 수정 시간도 생성 시간과 같음
    private final String description;

    // [요구사항] PRIVATE/PUBLIC 구분을 위한 필드와 참여자 목록 필드 추가
    private final boolean isPrivate;
    private final List<UUID> userIds;

    // 기존 PUBLIC 생성자 (기존 로직 유지용)
    public Channel(String channelTitles, String description) {
        this.id = UUID.randomUUID();
        this.channelTitles = channelTitles;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.description = description;
        this.isPrivate = false; // 기본은
        this.userIds = new ArrayList<>();
    }

    // PRIVATE 채널 전용 생성자 (오버로딩)
    public Channel(String channelTitles, String description, boolean isPrivate) {
        this.id = UUID.randomUUID();
        this.channelTitles = channelTitles;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.description = description;
        this.isPrivate = isPrivate;
        this.userIds = new ArrayList<>();
    }

    // 유저들을 채널에 참여시키는 메서드
    public void assignUsers(List<UUID> userIds) {
        if (userIds != null) {
            this.userIds.addAll(userIds);
        }
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