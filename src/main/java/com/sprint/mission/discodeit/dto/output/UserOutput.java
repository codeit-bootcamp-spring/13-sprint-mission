package com.sprint.mission.discodeit.dto.output;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Builder
public class UserOutput {
    private UUID id;
    private String name;
    private String email;
    private Boolean online;

    @Override
    public String toString() {
        return id + "," + name + "," + email + "," + online;
    }

}
