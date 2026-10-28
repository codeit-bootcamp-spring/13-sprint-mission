package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.MessageFile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageFileRepository extends JpaRepository<MessageFile, UUID> {
    boolean existsByMessage_Id(UUID messageId);

    void deleteByMessage_Id(UUID messageId);

    @EntityGraph(attributePaths = {"binaryContent"})
    List<MessageFile> findAllByMessage_Id(UUID messageId);

    @EntityGraph(attributePaths = {"binaryContent", "message"})
    @Query("SELECT mf FROM MessageFile mf WHERE mf.message.id IN :messageIds")
    List<MessageFile> findAllByMessageIdIn(@Param("messageIds") List<UUID> messageIds);

    @EntityGraph(attributePaths = {"binaryContent"})
    @Query("""
            select mf
            from MessageFile mf
            where mf.message.channel.id = :channelId
            """)
    List<MessageFile> findAllByChannelId(@Param("channelId") UUID channelId);

    @Modifying(flushAutomatically = true)
    @Query(value = """
                insert into message_files(
                            id,
                            message_id,
                            file_id,
                            created_at
                        )
                        select
                                gen_random_uuid(),
                                :messageId,
                                b.id,
                                now()
                        from binary_contents b
                        where b.id in (:fileIds)
            """, nativeQuery = true)
    int bulkInsert(@Param("fileIds") List<UUID> fileIds, @Param("messageId") UUID messageId);

    boolean existsByMessage_Channel_Id(UUID channelId);

    void deleteAllByIdIn(List<UUID> messageFileIds);

    /**
     * 파일에서 메시지 연결을 따라 소속 채널을 조회한다.
     * 현재 파일을 한 채널에 연결하는 업무 흐름을 전제로 Optional 단건을 반환한다.
     */
    @Query("""
        SELECT c
        FROM Channel c
        WHERE EXISTS (
                SELECT mf
                FROM MessageFile mf
                WHERE mf.binaryContent.id = :binaryContentId
                    AND mf.message.channel.id = c.id
            )
    """)
    Optional<Channel> findChannelByBinaryContentId(@Param("binaryContentId") UUID binaryContentId);
}
