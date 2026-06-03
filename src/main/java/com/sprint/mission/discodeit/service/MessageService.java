package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Message;


import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageService {


    // User: UUID id, Long createdAt, updatedAt, String username, email
    // Channel: UUID id, Long createdAt, updatedAt, ChannelType type, String name
    // Message: UUID id, Long createdAt, updatedAt, String content, UUID channelId

    // 생성, 읽기, 모두 읽기, 수정, 삭제 기능을 인터페이스로 선언
    // [도메인 모델 이름]Service 인터페이스 네이밍 규칙

    // 생성
    Message createOne(UUID channelId, String content, Long createdAt) throws IOException;

    // 읽기
    Optional<Message> readOne(UUID id) throws IOException;
    List<Message> readAll() throws IOException;

    // 수정
    Message editOne(UUID id,  UUID newChannelId, String newContent, Long updatedAt) throws IOException;


    // 삭제
    void deleteOne(UUID id) throws IOException; // 삭제는 반환할 값이 없기 때문에 void





}
