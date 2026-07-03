package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import lombok.Getter;
import lombok.ToString;


@Getter
@ToString(exclude = "password")
public class User extends BaseUpdatableEntity {

    private String username;
    private String email;
    private String password;
    private BinaryContent profile;
    private UserStatus status;


    public User (String username, String email, String password,  BinaryContent profile, UserStatus status) {
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
