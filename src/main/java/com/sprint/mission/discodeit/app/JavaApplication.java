package com.sprint.mission.discodeit.app;

import com.sprint.mission.discodeit.entity.User;

public class JavaApplication {

    //구현 테스트
    //등록
    //조회
    //수정
    //수정된 데이터 조회
    //삭제
    //조회를 통해 삭제되었는지 확인

    public static void main(String[] args) {
        User user1 = new User("박경석","pks@naver.com", "1234");
        System.out.println("이름: " + user1.getName() + " 이메일: " + user1.getEmail() + " 비밀번호: " + user1.getPassword());
    }
}
