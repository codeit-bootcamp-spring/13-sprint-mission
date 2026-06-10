package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.*;

//MessageService를 실제로 동작시키는 JCF(컬렉션) 기반 구현체
public class JCFMessageService implements MessageService {

    private final Map<UUID, Message> data; //메시지 데이터를 저장하는 메모리 저장소

    public JCFMessageService() {this.data = new HashMap<>();}

    @Override //메시지 생성
    public Message create(String content, UUID channelId, UUID authorId) {
        Message message = new Message(content, channelId, authorId);
        this.data.put(message.getId(), message);
        return message;
    }

    @Override // 메시지 단건조회
    public Message find(UUID messageId) {
        Message messageNullable = this.data.get(messageId);

        return Optional.ofNullable(messageNullable)
                .orElseThrow(()-> new NoSuchElementException("Message with id "  + messageId + " not found"));
    }

    @Override //전체 메시지 조회
    public List<Message> findAll() {
        return this.data.values().stream().toList();
    }

    @Override //메시지 수정
    public Message update(UUID messageId, String newContent) {
        Message messageNullable = this.data.get(messageId);
        Message message = Optional.ofNullable(messageNullable)
                .orElseThrow(()-> new NoSuchElementException("Message with id " + messageId + " not found"));
        message.update(newContent);
        return message;
    }

    @Override //메시지 삭제
    public void delete(UUID messageId) {
        if (!this.data.containsKey(messageId)) {
            throw new NoSuchElementException("Message with id " + messageId + " not found");
        }
        this.data.remove(messageId);
    }
}

