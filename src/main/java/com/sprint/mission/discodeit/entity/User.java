package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseUpdatableEntity {

    // 사용자 이름
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    // 사용자 이메일
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    // 사용자 비밀번호
    @Column(name = "password", nullable = false, length = 60)
    private String password;

    /*
     * 프로필 이미지 BinaryContent id
     *
     * 현재 서비스 코드가 profileId를 UUID로 다루고 있으므로
     * 우선 기존 구조와 호환되도록 UUID 필드로 유지한다.
     *
     * 추후 완전한 JPA 연관관계로 전환한다면
     * BinaryContent profile 필드와 @OneToOne 관계로 바꿀 수 있다.
     */
    @Column(name = "profile_id")
    private UUID profileId;

    public User(String username, String email, String password) {
        validate(username, email, password);

        this.username = username;
        this.email = email;
        this.password = password;
        this.profileId = null;
    }

    public void update(String username, String email, String password) {
        validate(username, email, password);

        this.username = username;
        this.email = email;
        this.password = password;
        markUpdated();
    }

    // profileId까지 함께 수정할 때 사용
    public void update(String username, String email, String password, UUID profileId) {
        validate(username, email, password);

        this.username = username;
        this.email = email;
        this.password = password;
        this.profileId = profileId;
        markUpdated();
    }

    public void updateProfileId(UUID profileId) {
        this.profileId = profileId;
        markUpdated();
    }

    private void validate(String username, String email, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("사용자 이름은 비어 있을 수 없습니다.");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 비어 있을 수 없습니다.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 비어 있을 수 없습니다.");
        }
    }
}