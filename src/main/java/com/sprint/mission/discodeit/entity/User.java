package com.sprint.mission.discodeit.entity;

//너무 내부 로직에 치우친 공부-> 메서드 호출 방향, 받은 값의 출처, 반환 값받을 누가 받는지 등을 더 참고.

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class User implements Serializable {//
    private static final long serialVersionUID = 1L;


    private UUID id;
    private Long createdAt;
    private Long updatedAt;
    private String userName;
    private String password;
    private String email;
    //생성자.
    //객체를 직접 생성하지는 않음, 생성된 객체에 필드 안에 변수를 채워 넣음.
    //객체를 사용하는 일반 메서드와 다르게 객체가 만들어지는 시점 단 한번 실행되며,
    // 객체에 고유성을 만드는 메서드이다.
    //메서드 이름이 클레스와 동일하면 jav는 이를 생성자로 인식함/ 생성자에는void,static 같은 타입 안 적음.
    //Message class에 매개변수와 객체의 인수에 관계를 설명함. 모르면 일단 읽어.
    //아마 너 나증에 분명 까먹을 테니까 걍 읽어 두번 읽고 한번 더 읽어.
    public User(String username, String password, String email) {//유저가 입력한 문자열을 받아서 {}를 실행
        this.id = UUID.randomUUID(); //randomUUID();이 만든 무작위 번호 할당
        this.createdAt = Instant.now().getEpochSecond(); //String username를 입력한 유닉스 타임 할당
        this.updatedAt = this.createdAt; //수정 시간을 유닉스타임과 같게 함.
        this.userName = username;//(String username)로 받은 정보를 할당
        this.password = password;
        this.email = email;

        /*
        이 메서드에서는 필드 변수 id, createdAt,updatedAt,username에 값을 할당하는데,
        this.가 있어서 각 메서드들이 만든 값들은 사용자 입력과 동시에 생성된 객체의 필드안에 할당이 된다.
        각 변수에 할당이 끝나면(값을 만드는 즉시) 메서드는 종료.
        필드 변수가 있는 public class User 를 기반으로 객체를 만들면 new User() 객체의 필드 안에 필드 변수의 공간이 할당 됨
         */
    }

    //getter메서드 만들기 표준 관습 get+필드명
    //생성자에서 객체에 할당한 값을 보여줌. 서비스 중에 운영자 또는 유저가 단순 정보를 조회할 때 뿐만 아니라
    //시스템 내부적으로 로그인시 실제 아이디와 비교하여 로그인처리를 결정할때,
    //특정 대상을 타겟하여 메세지를 전송하는 등 객체의 필드 안에있는 고유 값을 이용하는
    //모든 상황에서 필요한 메서드이다.
    public UUID getId() {return id;}
    public Long getCreatedAt() {return createdAt;}
    public Long getUpdatedAt() {return updatedAt;}
    public String getUserName() {return userName;}
    public String getPassword() {return password;}
    public String getEmail() {return email;}


    //update 메서드는 구현체인 JCFUserService에게 매개변수를 할당 받는다.
    public void update(String newUsername, String newEmail, String newPassword) {
        boolean anyValueUpdated = false;
        if(newUsername != null && !newUsername.equals(this.userName)) {
            this.userName = newUsername;
            anyValueUpdated = true;
        }
        if (newEmail != null && !newEmail.equals(this.email)) {
            this.email = newEmail;
            anyValueUpdated = true;
        }
        if (newPassword != null && !newPassword.equals(this.password)){
            this.password = newPassword;
            anyValueUpdated = true;
        }
        if (anyValueUpdated) {
            this.updatedAt = Instant.now().getEpochSecond();
        }

    }
}