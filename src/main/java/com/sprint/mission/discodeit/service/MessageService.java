package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    Message create (Message message);
    Message findByContent(String content);
    List<Message> findAll();
    void update(Message message);
    void delete(String content);
}

// [ ] 도메인 모델 별 CRUD(생성, 읽기, 모두 읽기, 수정, 삭제) 기능을 인터페이스로 선언하세요

/*
[ ] 등록 -> 메세지 전송
[ ] 조회(단건, 다건) -> 메세지 검색(특정 메세지 검색/전체 메세지 로딩)
[ ] 수정 -> 메세지 내용 수정
[ ] 수정된 데이터 조회 -> 메세지 재검색
[ ] 삭제 -> 메세지 삭제
[ ] 조회를 통해 삭제되었는지 확인 -> 메세지 재검색
 */
