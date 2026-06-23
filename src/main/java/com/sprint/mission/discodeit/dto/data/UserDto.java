package com.sprint.mission.discodeit.dto.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

// 사용자 정보를 전달하기 위한 DTO(Data Transfer Object)
@Getter //모든 필드에 대한 Getter메서드 자동생성
@NoArgsConstructor //기본 생성자(UserDTO()) 자동생성
@AllArgsConstructor //모든 필드를 매개변수로 받는 생성자 자동 생성
public class UserDto {
    private UUID id; // 시스템 전체에게 사용자를 구분하는 값
    private Instant createdAt; //사용자 생성 시각
    private Instant updatedAt; //사용자전보 최종 수정 시각
    private String username; //사용자 이릌(닉네임 또는 사용자명)
    private String email; //사용자 이메일
    private UUID profileId; //프로필 정보의 고유 식별자
    private Boolean online; //사용자 온라인 상태
}
