package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Message;
import java.util.UUID;
import java.util.List;

//Message 엔티티(메시지)용 CRUD 기능 인터페이스
public interface MessageService {
    void create(Message message); // (C) 메시지 생성
    Message read(UUID id); // (R) 아이디로 메시지 한 개 조회
    List<Message> readAll(); // (R) 모든 메시지 리스트 조회
    void update(Message message); //(U) 메시지 내용 수정
    void delete(UUID id); // (D) 메시지 삭제
}
