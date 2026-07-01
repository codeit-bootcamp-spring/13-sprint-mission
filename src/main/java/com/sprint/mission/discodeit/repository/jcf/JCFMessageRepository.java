package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
@Repository
//MessageRepository 인터페이스의 JCF(java collection Framwork) 구현체
//실제 데이터베이스(DB)를 사용하지 않고 java메모리(map)에 Message 객체를 저장
//프로그램이 실행되는 동안만 데이터가 유지되며 프로그램 종료 시 모든 데이터가 사라짐.
public class JCFMessageRepository implements MessageRepository {
    private final Map<UUID, Message> data; //메모리 저장소
    public JCFMessageRepository() {this.data = new HashMap<>();} //생성자. HasMap 기반 메모리 저장소 초기화

    @Override //메시지 저장
    public Message save(Message message) {
        this.data.put(message.getId(), message);
        return message;
    }

    @Override //메시지 id로 조회
    public Optional<Message> findById(UUID id) {return Optional.ofNullable(this.data.get(id));}

    @Override //특정 채널의 모든 메시지 조회
    public List<Message> findAllByChannelId(UUID channelId) {
        return this.data.values().stream().filter(message->message.getChannelId().equals(channelId)).toList();
    }

    @Override //특정 메시지 존재 여부 확인
    public boolean existsById(UUID id) {return this.data.containsKey(id);}

    @Override //메시지 삭제
    public void deleteById(UUID id) {this.data.remove(id);}

    @Override //특정 채널의 모든 메시지 삭제
    public void deleteAllByChannelId(UUID channelId) {
        this.findAllByChannelId(channelId)
                .forEach(message -> this.deleteById(message.getId()));
    }
}
