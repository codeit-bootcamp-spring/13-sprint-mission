package com.sprint.mission.discodeit.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class UpdateUserStatusRequest {

    private UUID userId;

}
