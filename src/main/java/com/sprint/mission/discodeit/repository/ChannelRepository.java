package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import java.util.UUID;
import java.util.List;

//Channel 객체를 저장하고 조히하기 위한 Repository 인터페이스
public interface ChannelRepository {
    Channel save(Channel channel); //채널 저장
    Channel findById(UUID id); //Id로 채널 조회
    List<Channel> findAll();
    void delete(UUID id);
}
