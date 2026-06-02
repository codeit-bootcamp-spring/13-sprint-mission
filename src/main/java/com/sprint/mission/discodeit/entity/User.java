package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

import static java.util.UUID.randomUUID;

public class User implements Serializable {

    private static final long serialVersionUID=1L;
    // 직렬화 및 역직렬화를 수행할 때 이 클래스의 버전을 의미


    // 객체 속성 필드
    private final UUID id; // 객체 식별
    // UUID 범용 고유 식별자, 중복 되지 않는 유일한 값
    private final Long createdAt;
    private Long updatedAt; // 각각 객체의 생성, 수정 시간을 유닉스 타임스탬프로 나타냄

    private String username, email; // 사용자 닉네임, 이메일 추가




    // 생성자 호출
    public User(String username, String email, Long createdAt){

        this(randomUUID(), username, email, System.currentTimeMillis());


    }

    public User(UUID id, String username, String email, Long createdAt){
        this.id=id; // id 초기화
        this.username=normalizeUserName(username);
        this.email= normalizeEmail(email);
        this.createdAt= createdAt; // 유낙스 타임스탬프 얻기
        this.updatedAt=createdAt;

    }



    // 필드 수정하는 update 함수 정의
    public void updateUser(String newUserName, String newEmail, Long updatedAt){

        this.username=normalizeUserName(newUserName);
        this.email= normalizeEmail(newEmail);
        this.updatedAt=System.currentTimeMillis();
        System.out.println("계정 정보\n사용자명: "+newUserName+"\n아이디: "+id+"\n이메일: "+newEmail+"\n최초 생성: "+createdAt+"\n수정: "+this.updatedAt);
        System.out.println("ESC...");
        System.out.println();


    }





    private String normalizeUserName(String newUserName){
        if(newUserName.isBlank()){
            return "조심하세요! 공백은 불가능합니다...";
        }
        return newUserName;
    }

    private String normalizeEmail(String newEmail){
        if(!newEmail.contains("@")){
            return "이메일 형식을 준수해주세요...";
        }
        return newEmail;
    }



    // Getter 함수 정의

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }





}
