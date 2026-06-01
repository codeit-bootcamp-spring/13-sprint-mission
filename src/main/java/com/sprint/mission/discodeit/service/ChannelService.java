package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    Channel create (Channel channel);
    Channel findById(UUID id);
    List<Channel> findAll();
    void update(Channel channel);
    void delete(UUID id);
}

// [ ] 도메인 모델 별 CRUD(생성, 읽기, 모두 읽기, 수정, 삭제) 기능을 인터페이스로 선언하세요

/*
[ ] 등록 -> 채널(대화창) 생성
[ ] 조회(단건, 다건) -> 채널 검색(특정 채널 검색/전체 채널 조회)
[ ] 수정 -> 채널 이름 수정
[ ] 수정된 데이터 조회 -> 채널 이름 재검색
[ ] 삭제 -> 채널 삭제
[ ] 조회를 통해 삭제되었는지 확인 -> 채널 이름 재검색
 */