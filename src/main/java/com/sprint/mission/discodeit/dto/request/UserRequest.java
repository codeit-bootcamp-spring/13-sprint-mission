package com.sprint.mission.discodeit.dto.request;

public record UserRequest(
        String username,
        String email,
        String password,
        String profileImageName
) {}
/*
프로필 이미지, username, email, 패스워드, 대체할 프로필 이미지
유저를 등록하기 위해 필요한 파라미터, 프로필 이미지를 등록하기 위해 필요한 파라미터, 수정 대상 객체의 id 파라미터(수정할 사용자의 id), 수정할 값 파라미터(이름, 이메일, 패스워드, 프로필이미지이름)
관련된 도메인도 같이 삭제: BinaryContent(프로필), UserStatus
 */