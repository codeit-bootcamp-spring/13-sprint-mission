package com.sprint.mission.discodeit.security.jwt;


import com.sprint.mission.discodeit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByHash(String hash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RefreshToken rs set rs.state = RefreshTokenStatus.REVOKED where rs.user = :user and rs.state in null")
    int revokeAllByUser(@Param("user") User user);
}
