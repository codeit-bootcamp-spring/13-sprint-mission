package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

//MessageService를 실제로 동작시키는 JCF(컬렉션) 기반 구현체
public class JCFMessageService implements MessageService {

    private final Map<UUID, Message> data; //메시지 데이터를 저장하는 메모리 저장소
    private final ChannelService channelService; //메시지가 작성될 채널을 조회하기 위한 서비스
    private final UserService userService; //메시지 작성자를 조회하기 위한 서비스

    public JCFMessageService(ChannelService channelService, UserService userService) {
        this.data = new HashMap<>();
        this.channelService = channelService;
        this.userService = userService;
    }

    @Override //메시지 생성
    public Message create(String content, UUID channelId, UUID authorId) {
        try { // 1.채널 존재 여부 확인, 2.사용자 존재여부확인, 둘중 하나라도 미존재 시 NoSuchElementException 발생
            channelService.find(channelId);
            userService.find(authorId);
        } catch (NoSuchElementException e) {
            throw e;
        }
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

