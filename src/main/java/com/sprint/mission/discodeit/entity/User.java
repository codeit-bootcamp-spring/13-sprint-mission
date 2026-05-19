package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id; // [요구사항] id는 생성자에서 초기화 // [요구사항] 내부에서 초기화
    private String username; // 이름은 밖에서 받아옴
    private final Long createdAt; // [요구사항] createdAt은 생성자에서 초기화 // [요구사항] 내부에서 초기화
    private Long updatedAt; // 처음 생성 시엔 수정 시간도 생성 시간과 같음

    public User(String username) {
        this.id = UUID.randomUUID();
        this.username = username; // 이름표에 적힌 글씨
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }

    public void updateName(User user) { // 이미 존재하는 유저의 이름을 바꾸는 기능
        this.username = user.getUsername();
        this.updatedAt = System.currentTimeMillis(); // 이름이 수정될 때 시간도 수정됨
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

}

/*
[ ] 등록 -> 사용자 등록
[ ] 조회(단건, 다건) -> 사용자 조회(특정 사용자 찾기/전체 목록 조회)
[ ] 수정 -> 사용자 정보(이름) 수정
[ ] 수정된 데이터 조회 -> 사용자 재검색
[ ] 삭제 -> 사용자 등록 삭제
[ ] 조회를 통해 삭제되었는지 확인 -> 사용자 재검색
 */