package com.sprint.mission.discodeit.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateProfileImageRequest {

        private String filename;
        private String contentType;
        private byte[] bytes;

}