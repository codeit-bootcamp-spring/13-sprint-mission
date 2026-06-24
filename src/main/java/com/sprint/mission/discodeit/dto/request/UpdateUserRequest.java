package com.sprint.mission.discodeit.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateUserRequest {

    // 기존에는 UUID id 필드를 포함했으나,
    // RESTful API 방식으로 변경하면서 id는 URL PathVariable로 전달받도록 수정
    private String username;
    private String email;
    private String password;

}
