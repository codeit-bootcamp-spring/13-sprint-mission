package com.sprint.mission.discodeit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

//비공개 채널 생성 요청 정보를 전달하기 위한 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PrivateChannelCreateRequest{
        private List<UUID> participantIds; //채널 생성 시 참여자로 등록될 사용자들의 식별자를 저잘한다.
}
