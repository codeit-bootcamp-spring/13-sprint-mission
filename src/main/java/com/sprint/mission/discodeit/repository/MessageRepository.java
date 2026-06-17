package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

//Messagae 엔티티의 데이터 저장, 조회, 삭제 기능을 정의하는 Repository 인터페이스
public interface MessageRepository {
    Message save(Message message); //메시지 저장
    Optional<Message> findById(UUID id); //Id로 메시지 조회
    List<Message> findAllByChannelId(UUID channelId); //전체 메시지 조회
    boolean existsById(UUID id); //메시지 존재 여부 확인
    void deleteById(UUID id); //메시지 삭제
    void deleteAllByChannelId(UUID channelId);
}
