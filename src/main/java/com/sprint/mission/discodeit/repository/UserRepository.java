package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("select u.id from User u")
    List<UUID> findAllIds();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"profile"})
    List<User> findAll();

    @EntityGraph(attributePaths = {"profile"})
    Optional<User> findById(UUID id);

    @EntityGraph(attributePaths = {"profile"})
    List<User> findByIdIn(List<UUID> ids);

    @EntityGraph(attributePaths = {"profile"})
    Optional<User> findByUsername(String username);

    boolean existsByRole(Role role);

    @Query("select u.id from User u where u.role = :role")
    List<UUID> findUserIdsByRole(@Param("role") Role role);

    /**
     * 프로필 업로드 상태의 개인 수신자를 찾기 위해 소유자 ID만 조회한다.
     */
    @Query("""
        SELECT u.id
        FROM User u
        WHERE u.profile.id = :profileId
    """)
    Optional<UUID> findUserIdByProfileId(@Param("profileId") UUID profileId);

}
