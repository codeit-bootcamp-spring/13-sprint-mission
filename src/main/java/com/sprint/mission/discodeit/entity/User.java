package com.sprint.mission.discodeit.entity;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class User extends BaseEntity {

    private final String email;
    private String password;
    private String name;
    private UUID profileID;

    // temp ToString
    @Override
    public String toString(){
        String res = " ===== User ====== \n"
                + "id : "  + this.getId() + "\n"
                + "createdAt : " + this.getCreatedAt() + "\n"
                + "updatedAt :" + this.getUpdatedAt() + "\n"
                + "name : " + this.name + "\n"
                + "userId :" + this.email + "\n"
                + "userPw : " + this.password + "\n";
        return res;
    }


}
