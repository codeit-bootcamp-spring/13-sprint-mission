package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

//Channel 엔티티(채널/방)용 CRUD 기능 인터페이스
public interface ChannelService {
    Channel create(PublicChannelCreateRequest request); // (C) 만들기 (공개 채널 생성)
    Channel create(PrivateChannelCreateRequest request); // 비공개 채널 생성
    ChannelDto find(UUID channelId); // (R) 한 명 조회 (아이디로 채널 한 개 조회)
    List<ChannelDto> findAllByUserId(UUID userId); // (R) 모두 조회 (모든 채널 리스트 조회)
    Channel update(UUID channelId, PublicChannelUpdateRequest request); // (U) 수정 (채널 정보 수정)
    void delete(UUID channelId); // (D)삭제 (채널 삭제)
}

