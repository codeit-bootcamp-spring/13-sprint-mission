package com.sprint.mission.discodeit.entity;


import com.sprint.mission.discodeit.security.role.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseUpdatableEntity {
    @Column(nullable = false,unique = true)
    private String username;
    @Column(nullable = false,unique = true)
    private String email;
    @Column(nullable = false)
    private String password;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id",unique = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private BinaryContent profile;

    @Enumerated(EnumType.STRING)
    @Column
    private Role role;

    public User (
            String username,
            String email,
            String password,
            BinaryContent profile,
            Role role
    ){
        this.username = username;
        this.email = email;
        this.password = password;
        this.profile = profile;
        this.role = role;
    }

    public void update(
            String username,
            String email,
            String password,
            BinaryContent profile
    ){
        if (username != null) this.username = username;
        if (email != null) this.email = email;
        if (password != null) this.password = password;
        if (profile != null) this.profile = profile;
    }

    public void updateRole(Role role){ this.role = role; }
}
