package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

    @EntityGraph(attributePaths = {"user", "channel"})
    @Query("SELECT rs FROM ReadStatus rs WHERE rs.user.id = :userId")
    List<ReadStatus> findByUserId(@Param("userId") UUID userId);

    void deleteByChannel_Id(UUID channelId);

    boolean existsByChannel_IdAndUser_Id(UUID channelId, UUID userId);

    @Modifying(flushAutomatically = true)
    @Query(value = """
            insert into read_statuses(
                            id,
                            channel_id,
                            user_id,
                            last_read_at,
                            created_at,
                            updated_at
                        )
                        select
                            gen_random_uuid(),
                            :channelId,
                            u.id,
                            :readAt,
                            now(),
                            now()
                            from users u 
                            where u.id in (:userIds)
            """,
            nativeQuery = true)
    int burkInsert(@Param("channelId") UUID channelId, @Param("userIds") List<UUID> userIds, @Param("readAt") Instant readAt);

    @EntityGraph(attributePaths = {"user", "user.profile", "channel"})
    @Query("SELECT rs FROM ReadStatus rs WHERE rs.channel.id = :channelId")
    List<ReadStatus> findByChannelId(@Param("channelId") UUID channelId);

    @EntityGraph(attributePaths = {"user", "user.profile", "channel"})
    @Query("SELECT rs FROM ReadStatus rs WHERE rs.channel.id IN :channelIds")
    List<ReadStatus> findByChannelIdIn(List<UUID> channelIds);

    boolean existsByChannel_Id(UUID channelId);

    @EntityGraph(attributePaths = {"user", "channel"})
    @Override
    Optional<ReadStatus> findById(UUID id);

    boolean existsByUser_Id(UUID userId);

    void deleteByUser_Id(UUID userId);
}
