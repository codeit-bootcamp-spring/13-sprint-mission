package com.sprint.mission.discodeit.dto.input;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Builder
public class UpdateUserInput {
    private String id;
    private String name;
    private String pw;
    private String thumbnail;

    public UUID getId(){
        return UUID.fromString(id);
    }

    public UUID getThumbnail(){
        return UUID.fromString(thumbnail);
    }

}
