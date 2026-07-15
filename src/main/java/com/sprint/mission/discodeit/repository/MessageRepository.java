package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Message 데이터를 저장하고 조회하기 위한 Spring Data JPA Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    // 특정 Channel에 작성된 메시지 목록 조회
    List<Message> findAllByChannelId(UUID channelId);

    /*
     * 커서 페이지네이션 첫 조회
     * 특정 Channel의 메시지를 최신순으로 size + 1개 조회할 때 사용
     */
    List<Message> findByChannelIdOrderByCreatedAtDesc(
            UUID channelId,
            Pageable pageable
    );

    /*
     * 커서 페이지네이션 다음 조회
     * cursor보다 오래된 메시지를 최신순으로 size + 1개 조회할 때 사용
     */
    List<Message> findByChannelIdAndCreatedAtLessThanOrderByCreatedAtDesc(
            UUID channelId,
            Instant cursor,
            Pageable pageable
    );

    // 특정 Channel의 가장 최근 메시지 생성 시간 조회
    @Query("""
            select max(m.createdAt)
            from Message m
            where m.channelId = :channelId
            """)
    Instant findLastMessageAtByChannelId(UUID channelId);

    // 특정 Channel에 작성된 모든 메시지 삭제
    void deleteByChannelId(UUID channelId);

    // 특정 User가 작성한 모든 메시지 삭제
    void deleteByAuthorId(UUID authorId);
}