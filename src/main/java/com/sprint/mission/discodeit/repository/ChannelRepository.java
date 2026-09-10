package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.querydsl.ChannelQueryDsl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ChannelRepository extends JpaRepository<Channel, UUID>, ChannelQueryDsl {
    List<Channel> findByTypeIs(ChannelType type);


    // todo - 삭제 예정. queryDsl 매서드로 변경
    // 조회 가능한 채널 쿼리
    // 1. 채널 타입이 public 인 경우
    // 2. 채널의 ReadStatus 에 User id 가 포함된 경우
    @Query(
            """
            select c
            from Channel c
            where c.type = 'PUBLIC' or exists (
                        select 1
                        from ReadStatus rs
                        where rs.user.id = :id and rs.channel.id = c.id
                        )
            """
    )
    List<Channel> findVisibleChannelByUserId(UUID id);

}
