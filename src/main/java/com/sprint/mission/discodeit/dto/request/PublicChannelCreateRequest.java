package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.ChannelType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//공개 채널 생성 요청 정보를 전달하기 위한 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PublicChannelCreateRequest{
        private ChannelType type; //채널 유형. 생성할 채널의 종류를 나타냄.
        private String name; //채널 이름
        private String description; //채널의 목적이나 사용요도를 설명하는 정보
}
