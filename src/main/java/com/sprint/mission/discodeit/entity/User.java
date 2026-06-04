package com.sprint.mission.discodeit.entity;

import java.io.*;

public class User extends BaseEntity implements Serializable {

    private String userName;
    private String email;
    private String passWord;

    public User(String userName, String email, String passWord) {
        super();
        validateUserName(userName);
        validateEmail(email);
        validatePassWord(passWord);
    }


    private void validateUserName(String userName) {
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        this.userName = userName;
    }

    public void updateUserName(String userName) {
        validateUserName(userName);

        this.userName = userName;
        setUpdatedAt();
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일 입력은 필수입니다.");
        }
        this.email = email;
    }

    public void updateEmail(String email) {
        validateEmail(email);

        this.email = email;
        setUpdatedAt();
    }

    public String getPassWord() {
        return passWord;
    }

    private void validatePassWord(String passWord) {
        if (passWord == null || passWord.isBlank()) {
            throw new IllegalArgumentException("비밀번호 입력 필수입니다.");
        }
        this.passWord = passWord;
    }

    public void updatePassWord(String passWord) {
        validatePassWord(passWord);

        this.passWord = passWord;
        setUpdatedAt();
    }

    @Override
    public String toString() {
        return "등록 정보: " +
                "이름 = '" + userName + '\'' +
                ", email = '" + email + '\'' +
                ", passWord = '" + passWord + '\'';
    }
}




