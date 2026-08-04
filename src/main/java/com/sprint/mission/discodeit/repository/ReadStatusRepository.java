package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

// ReadStatus 데이터를 저장하고 조회하기 위한 Spring Data JPA Repository
public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

    /*
     * 특정 User와 특정 Channel에 해당하는 ReadStatus 조회
     *
     * ReadStatus 엔티티가 userId, channelId UUID 필드가 아니라
     * User user, Channel channel 연관관계를 가지므로 user.id, channel.id 기준으로 조회한다.
     */
    ReadStatus findByUser_IdAndChannel_Id(UUID userId, UUID channelId);

    /*
     * 특정 User의 모든 ReadStatus 조회
     *
     * ReadStatus.user.id 기준으로 조회한다.
     */
    List<ReadStatus> findAllByUser_Id(UUID userId);

    /*
     * 특정 Channel의 모든 ReadStatus 조회
     *
     * ReadStatus.channel.id 기준으로 조회한다.
     */
    List<ReadStatus> findAllByChannel_Id(UUID channelId);

    /*
     * 특정 User가 참여한 Channel id 목록 조회
     *
     * ReadStatus.channel.id를 조회한다.
     */
    @Query("""
            select r.channel.id
            from ReadStatus r
            where r.user.id = :userId
            """)
    List<UUID> findChannelIdsByUserId(UUID userId);

    /*
     * 특정 Channel에 참여한 User id 목록 조회
     *
     * ReadStatus.user.id를 조회한다.
     */
    @Query("""
            select r.user.id
            from ReadStatus r
            where r.channel.id = :channelId
            """)
    List<UUID> findUserIdsByChannelId(UUID channelId);

    /*
     * 특정 Channel과 관련된 모든 ReadStatus 삭제
     *
     * ReadStatus.channel.id 기준으로 삭제한다.
     */
    void deleteByChannel_Id(UUID channelId);

    /*
     * 특정 User와 관련된 모든 ReadStatus 삭제
     *
     * ReadStatus.user.id 기준으로 삭제한다.
     */
    void deleteByUser_Id(UUID userId);
}