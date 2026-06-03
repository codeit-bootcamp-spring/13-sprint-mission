package com.sprint.mission.discodeit.entity;

public class User extends CoreEntity {

    // 필드
    private String userName;
    private String password;
    private String email;
    private String phoneNumber;

    // 생성자
    public User(String userName, String password, String email, String phoneNumber) {
        super();
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // setter 대신 update
    public void updateUserName(String newUserName) {
        this.userName = newUserName;
        this.update();
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
        this.update();
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
        this.update();
    }

    public void updatePhoneNumber(String newPhoneNumber) {
        this.phoneNumber = newPhoneNumber;
        this.update();
    }

    // getter
    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

}