package com.sprint.mission.discodeit.entity;

public class User extends BaseEntity {

    private String userName;
    private String email;
    private String passWord;

    public User(String userName, String email, String passWord) {
        super();
        this.userName = validateName(userName);
        this.email = validateEmail(email);
        this.passWord = validatePassWorld(passWord);
    }

    public void setUserName(String userName) {
        this.userName = validateName(userName);
        setUpdatedAt();
    }


    private String validateName(String newUserName) {
        if (newUserName == null || newUserName.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        return newUserName;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = validateEmail(email);
        setUpdatedAt();
    }

    private String validateEmail(String newEmail) {
        if (newEmail == null || newEmail.isBlank()) {
            throw new IllegalArgumentException("이메일 입력은 필수입니다.");
        }
        return newEmail;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = validatePassWorld(passWord);
        setUpdatedAt();
    }

    private String validatePassWorld(String NewPassWord) {
        if (NewPassWord == null || NewPassWord.isBlank()) {
            throw new IllegalArgumentException("비밀번호 입력 필수입니다.");
        }
        return NewPassWord;
    }

    @Override
    public String toString() {
        return "등록 정보: " +
                "이름 = '" + userName + '\'' +
                ", email = '" + email + '\'' +
                ", passWord = '" + passWord + '\'';
    }
}




