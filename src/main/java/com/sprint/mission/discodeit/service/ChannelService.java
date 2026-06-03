package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelService {

    // User: UUID id, Long createdAt, updatedAt, String username, email
    // Channel: UUID id, Long createdAt, updatedAt, ChannelType type, String name
    // Message: UUID id, Long createdAt, updatedAt, String content, UUID channelId

    // 생성, 읽기, 모두 읽기, 수정, 삭제 기능을 인터페이스로 선언
    // [도메인 모델 이름]Service 인터페이스 네이밍 규칙

    // 생성
    Channel createOne(ChannelType type, String name, Long createdAt) throws IOException;

    // 읽기
    Optional<Channel> readOne(UUID id) throws IOException;
    List<Channel> readAll() throws IOException;

    // 수정
    Channel editOne(UUID id, ChannelType newType, String newName, Long updatedAt) throws IOException;


    // 삭제
    void deleteOne(UUID id) throws IOException; // 삭제는 반환할 값이 없기 때문에 void




}
