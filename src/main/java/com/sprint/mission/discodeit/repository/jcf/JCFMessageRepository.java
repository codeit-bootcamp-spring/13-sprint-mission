package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.*;

//MessageRepository 인터페이스의 JCF(java collection Framwork) 구현체
//실제 데이터베이스(DB)를 사용하지 않고 java메모리(map)에 Message 객체를 저장
//프로그램이 실행되는 동안만 데이터가 유지되며 프로그램 종료 시 모든 데이터가 사라짐.
public class JCFMessageRepository implements MessageRepository {
    private final Map<UUID, Message> data; //메시지 데이터를 저장하는 map
    public JCFMessageRepository() {this.data = new HashMap<>();} //Repository 생성 시 HashMap

    @Override //메시지 저장
    public Message save(Message message) {
        this.data.put(message.getId(), message);
        return message;
    }

    @Override //id로 메시지 조회
    public Optional<Message> findById(UUID id) {return Optional.ofNullable(this.data.get(id));}

    @Override //전체 메시지 조회
    public List<Message> findAll() {return this.data.values().stream().toList();}

    @Override //메시지 존재 여부 확인
    public boolean existsById(UUID id) {return this.data.containsKey(id);}

    @Override //메시지 삭제
    public void deleteById(UUID id) {this.data.remove(id);}
}
