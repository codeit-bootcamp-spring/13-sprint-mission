package com.sprint.mission.discodeit.dto.output;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Builder
public class UserState {
    private String name;
    private String email;
    private Boolean online;
}
