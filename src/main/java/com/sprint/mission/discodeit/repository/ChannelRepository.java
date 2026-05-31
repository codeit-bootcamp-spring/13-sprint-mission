package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelRepository {

    Channel create (Channel channel);
    Channel findById(UUID id);
    List<Channel> findAll();
    void update(Channel channel);
    void delete(UUID id);

}
/*
레포지토리 설계 및 구현
[ ] "저장 로직"과 관련된 기능을 도메인 모델 별 인터페이스로 선언하세요.
 */