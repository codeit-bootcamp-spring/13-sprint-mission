package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> , MessageRepositoryCustom{

    @Override
    @EntityGraph(attributePaths = {"author", "channel", "author.profile"})
    Optional<Message> findById(UUID id);

    Optional<Message> findTop1ByChannel_IdOrderByCreatedAtDesc(UUID channelId);

    void deleteAllByChannel_Id(UUID channelId);

    boolean existsByChannel_Id(UUID channelId);

    @Modifying(flushAutomatically = true)
    @Query("""
        update Message m
        set
            m.author = null 
        where
            m.author.id = :authorId
               
    """)
    void detachAuthorByAuthorId(@Param("authorId") UUID authorId);

    boolean existsByAuthor_Id(UUID authorId);

    @EntityGraph(attributePaths = {"author"})
    @Query("SELECT m FROM Message m WHERE m.id = :messageId")
    Optional<Message> findWithAuthor(@Param("messageId") UUID messageId);
}
