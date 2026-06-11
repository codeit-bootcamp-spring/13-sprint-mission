package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

//Messagae 객체를 저장하고 조히하기 위한 Repository 인터페이스
public interface MessageRepository {
    Message save(Message message); //메시지 저장
    Optional<Message> findById(UUID id); //Id로 메시지 조회
    List<Message> findAll();
    boolean existsById(UUID id);
    void deleteById(UUID id);
}
