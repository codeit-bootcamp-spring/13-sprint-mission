package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.entity.Message;
import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//Messagae 엔티티의 데이터 저장, 조회, 삭제 기능을 정의하는 Repository 인터페이스
public interface MessageRepository {

  Message save(Message message); //메시지 저장

  Optional<Message> findById(UUID id); //Id로 메시지 조회

  List<MessageDto> findAllByChannelId(UUID channelId, Pageable pageable); //전체 메시지 조회

  boolean existsById(UUID id); //메시지 존재 여부 확인

  void deleteById(UUID id); //메시지 삭제

  void deleteAllByChannelId(UUID channelId); //특정 채널에 속한 모든 메시지 삭제
}
