package com.sprint.mission.discodeit.entity;


import lombok.Getter;

@Getter
public class User extends BaseEntity {
    private String name;
    private String userId;
    private String userPw;

    // 기타 사용자 프로필 정보.
    public User(String name, String userId, String userPw) {
        super();
        this.name = name;
        this.userId = userId;
        this.userPw = userPw;

        //this.joinedChannel = new ArrayList<>()
    }

    // temp ToString
    @Override
    public String toString(){
        String res = " ===== User ====== \n"
                + "id : "  + this.getId() + "\n"
                + "createdAt : " + this.getCreatedAt() + "\n"
                + "updatedAt :" + this.getUpdatedAt() + "\n"
                + "name : " + this.name + "\n"
                + "userId :" + this.userId + "\n"
                + "userPw : " + this.userPw + "\n";
        return res;
    }


    public void setName(String name){
        this.name = name;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }

    public void setUserPw(String userPw){
        this.userPw = userPw;
    }


}
