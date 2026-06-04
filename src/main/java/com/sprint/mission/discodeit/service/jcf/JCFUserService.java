package com.sprint.mission.discodeit.service.jcf;


import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//멘토님의 조언 듣기 전 공부 흔적.
public class JCFUserService implements UserService {

    private final List<User> data = new ArrayList<>();
    //new ArrayList<>(); 배열을 내부적으로 사용하는 객체를 만든다 근데 자동으로 칸이 알아서 늘어남. 그냥 배열과는 다름!
    //자바가 기본으로 제공하는 리스트의 한 종류
    //new ArrayList<>();의 <>안에는 배열에 들어갈 데이터 타입을 적지만 = 앞에 변수 선언에 이미 적어서 생략
    //final로 변수의 값이 재할당되는 것을 방지 재할당 되면 기존 데이터는 삭제됨 즉 리스트 삭제.
    //List<User>는 타입
    //List<User>타입의 data라는 변수를 선언 하는데 final로 변수에 선언된 객체를 변경하지 못 하게 고정한다.
    //new ArrayList<>();객체를 생성하고 data 변수에 할당한다.
    //Array<>() 변수 안에는 .add(), .remove(), .get() 등의 메서드가 정의 되어있다.
    //즉 길이가변하는 배열에 유저의 여러 정보를 넣을 수 있는 배열이면서 여러 도구가 들어있는 만능 도구함을 쓸거야~ 라는 뜻
    //


    @Override//어노테이션, 이 메서드는 인터페이스에서 물려받은 추상 메서드 재정의 할거야~
    public void create(User user) {//객체 생성 로직아래(메인클레스) 이 메서드를 호출하는 코드 추가.
        data.add(user);//외부에서 만든 객체를 리스트 내부에 저장한다.
                      //사용자가 id,pw를 입력해서 회원 가입하면 새로운 유저 객체가 생겨남, User클레스에 생성자에 메서드를 따라 유저
        // 객체에는 이름 비번 생성시간 업뎃시간이 필드 변수로 담기고 이런 정보를 담고있는 유저 객체의 주소값을 리스트에 추가함.
        //이렇듯 신규로 가입 즉 새로운 유저 객체 생성시에는 객체의 필드 변수 초기화와 리스트에 해당 객체의 주소가 저장되는 일이


    }

    @Override
    public User read(UUID id) {
        for (User user : data) {//향상된 for문 좌항에 변수에 우항 안에 있는 배열을 0번째 칸부터 가장 마지막 칸에 있는 값을 순차적으로
                                //할당 하면서 {}안에 if문을 돌린다.
                                //User user 우항에 뱐수가 User타입이여야 하는 이유는 리스트에 제네릭타입이 User라서
                                //리스트 안에 객체 타입이 User만 있고,User 타입은 User타입 변수에만 할당이 가능하다
            if (user.getId().equals(id)) {
                return user;
                                //for(...)안에서 user변수에 순차적으로 할당되는 리스트 안에 객체에서 getId()를 통해 id를 뽑아온다.
                                //뽑아온 id를 .epuals(메서드의 매개 변수)와 동일한지 비교한다.
                                // 동일하면 for(...)안에서 user변수에 할당된 data안에 있는 유저객체를 리턴한다.
            }
        }return null;           //data에서 순차적으로 할당한 값 중에 매개변수로 들어온 갑과 같은게 없으면 null반환.
    }

    @Override
    public List<User> readAll() {
        return data;//자동완성으로 만들면 List.fo()가 나오는데 이건 일단 오류는 없게 해줄게~라는 의미로
                    //아무것도 없는 빈 리스트를 새로 만들어서 반환하는 메서드이다.
                    //실제 원하는 기능은 리스트 내부 전체 목록을 반환하는 것이니까 리스트가 할당된 변수 data를 써야한다.
    }

    @Override
    public void update(UUID id, String name, String email, String password) {
        User foundUser = read(id); //사용자가 네임(뭘 바꾸든 동일)을 바꿨다면,
                    //여기서는 UUID id만 인수로 받아옴
                    //그리고 read(id)로 UUID id를 리스트 내에서 찾아 본다.
                    //찾아서 User foundUser변수에 할당
                    // (결국 리스트 에서 유저 타입 객체를 찾은 거기 땨문에 유저 타입 변수에 할당)
        //중요! read(id)는 read(UUID id)를 호출한 거다. 즉 반복문을 사용.
        //선언 시에는 타입과 변수를 ()안에 쓰고 호출할때는 타입은 제외하고 변수만 ()안에 쓴다.
        //read(UUID id)는 메서드 선언 read(id)는 메서드 호출.

        if (foundUser != null) {//foundUser변수에 값이 null이 아니면 실행
            // null인 경우는 리스트가 비어있다는 거니까 id를 삭제 한 경우임.
            //또는 어떤 이유로 갑이 비어있을때 프로그램 다운을 막기 위해 메서드 정상 종료(0)하려고.
            foundUser.update(name, email, password);
            // (UUID id, String name, String email, String password)매개변수로 들어온
            //값을 update메서드에 전달. 변한거 안 변한 일단 전부 전달한다.

        }
    }

    @Override
    public void delete(UUID id) {
        User foundUser = read(id);
        if (foundUser != null) {
            //비정상 종료(1) 방지 널이 들어오면 걍 메서드 정상 종료
            //근데 ArrayList의 메서드 data는 널이 들어오면 안전하게 정상 종료를 함
            //사실 굳이 안정 장치를 만들 필요가 없긴 함.
       data.remove(foundUser);
        }// data는 ArrayList 객체를 담는 참조 변수임
        //foundUser변수 안에 들어간 유저 객체가 리스트에서 삭제됨.
        //정확히는 유저 객체를 담고있는 참조변수 User타입의 user변수가 삭제됨
        //참조 변수가 없는 객체는 우엇하고도 연결이 되있지 않게 되어 GC가
        //소리소문 없이 처리해버림.

    }
}
