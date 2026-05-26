package com.sprint.mission.discodeit.entity;

//너무 내부 로직에 치우친 공부-> 메서드 호출 방향, 받은 값의 출처, 반환 값받을 누가 받는지 등을 더 참고.

import java.io.Serializable;
import java.util.UUID;
//멘토님의 조언 듣기 전 공부 흔적.
//신규 가입자 한정으로 즉, 새로운 유저 객체가 생성 될때만 유저가 입력한 값이 이 클레스로 오고,
//이미 리스트에 올라가있는 즉, 객체가 이미 존재하는 유저가 필드 변수의 값을 변경할때는
//유저가 입력한 값들이 반드시 JCFUserService를 먼저 지나서 이 클레스로 온다. 그 뒤에 필드변수 재할당.
//어디서나 이용 가능한 클레스 선언.-> 유저 객체 생성시 사용해야하니까~
//유저의 정보는 프라이빗으로 선언-> 캡슐화(getter,update로만 소통함)
//필드 변수로 선언.--> 생성자에서 객체에 할당 할거임
//객체안에 할당된 변수는 객체가 메모리에 살아있는 동안 GC가 삭제 안 함.
public class User implements Serializable {//
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
        this.createdAt = System.currentTimeMillis(); //String username를 입력한 유닉스 타임 할당
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

    //필드를 수정하는 업데이트 함수
    //맨 아래 update메서드를 통해서 변경된 값을 받는다.
    //String newName은 객체를 생성하지 않고 기존 객체를 추적해서 업데이트 메서드에서
    //나온 값을 기존 객체의 필드 내부 변수와 교체한다.
    // 현제 업데이트 메서드는 변경된 이름과 변경 시간을 기존 객체의 네임 변수와 updateAt변수에 재 할당한다.
    public void updateName(String newName){
        this.userName = newName;
        this.updatedAt = System.currentTimeMillis();
    }
    public void updatePassword(String newPassword){
        this.password = newPassword;
        this.updatedAt = System.currentTimeMillis();
    }
    public void updateEmail(String newEmail){
        this.email = newEmail;
        this.updatedAt = System.currentTimeMillis();
    }

    //update 메서드는 구현체인 JCFUserService에게 매개변수를 할당 받는다.
    public void update(String name, String email, String password) {
        this.updateName(name);       //각각 위에있는
        this.updateEmail(email);     //updateName,updateEmail,updatePassword 메서드에
        this.updatePassword(password);  // 값을 할당한다.


    }
}