package com.sprint.mission.discodeit.entity;

import java.io.Serializable;

public class User extends BaseEntity implements Serializable {
    public static final long serialVersionUID = 1L;


    private String username;
    private final String email;
    private String password;
    private UserStatus userStatus;

    // 생성자
    public User(String username, String email, String password, UserStatus userStatus) {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
        this.userStatus = userStatus;
    }

    // getter (private 시 필요)

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    // update 함수 (닉네임, 패스워드, 접속상태)
    public void updateUsername(String username) {
        validateUsername(username);
        this.username = username;
        updateTimestamp();
    }
    public void updatePassword(String password) {
        validatePassword(password);
        this.password = password;
        updateTimestamp();
    }
    public void updateUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
        updateTimestamp();
    }

    // 수정 시 예외처리 (공백 검사)
    private void validateUsername(String username){
        if(username == null || username.isBlank()){
            throw new IllegalArgumentException("입력 칸을 비워두거나 공백을 사용할 수 없습니다.");
        }
    }

    private void validatePassword(String password){
        if(password == null || password.isEmpty()){
            throw new IllegalArgumentException("입력 칸을 비워두거나 공백을 사용할 수 없습니다.");
        }
    }


    @Override
    public String toString() {
        return  "| ID : " + getId() + '\n' +
                "| Name : " + username + '\n' +
                "| Email : " + email + '\n' +
                "| Password : " + password + '\n' +
                "| Status : " + userStatus + '\n' +
                "| Create : " + formatDate(getCreatedAt()) + '\n' +
                "| Last Update : " + formatDate(getUpdatedAt()) + '\n';
    }




}
