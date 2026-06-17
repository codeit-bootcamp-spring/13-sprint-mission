package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

//Channel 엔티티의 데이터 저장 및 조히 기능을 정의하는 Repository 인터페이스
public interface ChannelRepository {
    Channel save(Channel channel); //채널 저장
    Optional<Channel> findById(UUID id); //Id로 채널 조회
    List<Channel> findAll(); //전체 채널 조회
    boolean existsById(UUID id); //채널 존재 여부 확인
    void deleteById(UUID id); //채널 삭제
}
