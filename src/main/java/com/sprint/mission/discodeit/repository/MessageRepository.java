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

    /*
     * 특정 Channel에 작성된 메시지 목록 조회
     *
     * Message 엔티티가 channelId UUID 필드가 아니라
     * Channel channel 연관관계를 가지므로 channel.id 기준으로 조회한다.
     */
    List<Message> findAllByChannel_Id(UUID channelId);

    /*
     * 특정 User가 작성한 메시지 목록 조회
     *
     * Message 엔티티가 authorId UUID 필드가 아니라
     * User author 연관관계를 가지므로 author.id 기준으로 조회한다.
     */
    List<Message> findAllByAuthor_Id(UUID authorId);

    /*
     * 커서 페이지네이션 첫 조회
     * 특정 Channel의 메시지를 최신순으로 size + 1개 조회할 때 사용
     */
    List<Message> findByChannel_IdOrderByCreatedAtDesc(
            UUID channelId,
            Pageable pageable
    );

    /*
     * 커서 페이지네이션 다음 조회
     * cursor보다 오래된 메시지를 최신순으로 size + 1개 조회할 때 사용
     */
    List<Message> findByChannel_IdAndCreatedAtLessThanOrderByCreatedAtDesc(
            UUID channelId,
            Instant cursor,
            Pageable pageable
    );

    /*
     * 특정 Channel의 가장 최근 메시지 생성 시간 조회
     *
     * Message.channel.id 기준으로 조회한다.
     */
    @Query("""
            select max(m.createdAt)
            from Message m
            where m.channel.id = :channelId
            """)
    Instant findLastMessageAtByChannelId(UUID channelId);

    /*
     * 특정 Channel에 작성된 모든 메시지 삭제
     *
     * Message.channel.id 기준으로 삭제한다.
     */
    void deleteByChannel_Id(UUID channelId);

    /*
     * 특정 User가 작성한 모든 메시지 삭제
     *
     * Message.author.id 기준으로 삭제한다.
     */
    void deleteByAuthor_Id(UUID authorId);
}