package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import java.util.List;
import java.util.UUID;

//Channel 엔티티(채널/방)용 CRUD 기능 인터페이스
public interface ChannelService {
    void create(Channel channel); // (C) 만들기 (채널 생성)
    Channel read(UUID id); // (R) 한 명 조회 (아이디로 채널 한 개 조회)
    List<Channel> readAll(); // (R) 모두 조회 (모든 채널 리스트 조회)
    void update(Channel channel); // (U) 수정 (채널 정보 수정)
    void delete(UUID id); // (D)삭제 (채널 삭제)
}
