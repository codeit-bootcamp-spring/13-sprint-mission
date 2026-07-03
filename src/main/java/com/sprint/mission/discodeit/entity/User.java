package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;


@Getter
@ToString(exclude = "password")
@Entity
@Table(name = "users")
public class User extends BaseUpdatableEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 60)
    private String password;

    @OneToOne
    @JoinColumn(name = "profile_id", unique = true, nullable = true)
    private BinaryContent profile;

    @OneToOne(mappedBy = "user")
    private UserStatus status;

    protected User() {}

    public User (String username, String email, String password, BinaryContent profile, UserStatus status) {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
        this.profile =  profile;
        this.status = status;
    }

    //이름 수정
    public void updateUserName(String username){
        this.username = username;
    }
    //이메일 수정
    public void updateUserEmail(String email){
        this.email = email;
    }
    //비밀번호 수정
    public void updateUserPassword(String password){
        this.password = password;
    }

    //프로필 이미지 수정
    public void updateUserProfileId(BinaryContent profile){
        this.profile = profile;
    }
}
