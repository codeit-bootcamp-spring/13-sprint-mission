package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;//User Class낄여오기
import java.util.List;//java가 기본으로 제공하는 패키기 낄여오기
import java.util.UUID;//위와 동일함

public interface UserService {

    //인터페이스는 메서드가 없다. 이게 특징.
    //인터페이스는 추상 메서드~ 메서드 내용은 구현체에서 오버라이딩~
    //인터페이스로 빈 상자를 구현 복수 개발자가병렬로 개발 후 JCF 구현채에 때려 박아서 기능 구현
    void create(User user);// 생성 어디에? date List에
    // 유저를 생성하는게 아니고, 새로운 유저가 생기면 기존 리스트에 유저의 정보를 넣는 거임~

    User read(UUID id);//유저의 정보 조회(읽기) 반환 값이 User라서 모든 정보 조회

    List<User> readAll();//모든 유저를 목록에 담아서 반환
    //데이터 리스트에 주는게 아니라 데이터 리스트에 정보를 밖으로 보여줌
    //관리 측면에서 유저가 몇명인지 누구누구있는지 등등을 알 수 있음.(유저 조회)

    void update(UUID id,String name, String email, String password);
    //변경하는 기능 UUID는 변경이 아니라 누구를 변경하는지 알려주는 식별 번호로 사용될 예정, 니머지는 변경 가능

    void delete(UUID id);//유저 삭제
}
