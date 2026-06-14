package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageRepository {

    Message create(Message message);
    Message findByContent(String content);
    List<Message> findAll();
    void update(Message requestMessage);
    void delete(String content);
    void deleteByChannelId(UUID channelId);
}
/*
레포지토리 설계 및 구현
[ ] "저장 로직"과 관련된 기능을 도메인 모델 별 인터페이스로 선언하세요.
 */