package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserRoleUpdateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserUpdateCommand;
import com.sprint.mission.discodeit.entity.base.UpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@ToString(exclude = {"password", "profile"}, callSuper = true)
public class User extends UpdatableEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 60)
    private String password;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private BinaryContent profile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    public User(UserCreateCommand command, BinaryContent profile) {
        this.username = command.username();
        this.password = command.password();
        this.email = command.email();
        this.profile = profile;
        this.role = Role.USER;
    }

    public void updateInfo(
            UserUpdateCommand command, BinaryContent profile
    ) {
        this.username = keepIfBlank(command.username(), username);
        this.password = keepIfBlank(command.password(), password);
        this.email = keepIfBlank(command.email(), email);
        this.profile = profile;
    }

    public boolean isProfileImageExist() {
        return profile != null;
    }

    public boolean hasEmail(String email) {
        return this.email.equals(email);
    }

    public boolean hasUsername(String username) {
        return this.username.equals(username);
    }

    private String keepIfBlank(String newValue, String oldValue) {
        return StringUtils.defaultIfBlank(newValue, oldValue);
    }

    public UUID getProfileId() {
        if (profile == null) return null;

        return profile.getId();
    }

    public void updateRole(UserRoleUpdateCommand command) {
        this.role = command.newRole();
    }
}
