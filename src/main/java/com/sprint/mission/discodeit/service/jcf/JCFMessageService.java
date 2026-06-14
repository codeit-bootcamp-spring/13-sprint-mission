package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;

import java.time.Instant;
import java.util.*;

public class JCFMessageService implements MessageService {

    private final List<Message> data = new ArrayList<>();


    @Override
    public MessageResponse create(MessageCreateRequest dto) { // 매개변수 선언, 유저 생성하는 기능 구현
        return new MessageResponse
                (UUID.randomUUID(), dto.channelId(), dto.senderId(), dto.content(),
                 Collections.emptyList(), Instant.now(), Instant.now());
    }

    @Override
    public Optional<MessageResponse> findById(UUID id) {
        // 💡 Optional 규칙 매칭
        return Optional.empty();
    }

    /*@Override
    public Message findByContent(String content) { // 단건 조회
        for (Message foundMessage : data) {
            if (foundMessage.getContent().equals(content)) {
                return foundMessage;
            }
        }
        return null;
    }*/

    @Override
    public List<MessageResponse> findAll(UUID id) { // 전체 조회
        return Collections.emptyList();
    }

    @Override
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        return Collections.emptyList();
    }

    @Override
    public MessageResponse update(UUID id, MessageUpdateRequest dto) {
        return new MessageResponse
                (id, UUID.randomUUID(), UUID.randomUUID(), dto.content(),
                 Collections.emptyList(), Instant.now(), Instant.now());
    }

    @Override
    public void delete(UUID id) {}


    /*@Override
    public MessageResponse update(UUID id, MessageUpdateRequest dto) {
        Message foundMessage = findByContent(requestMessage.getContent());
        if (foundMessage != null) {
            foundMessage.updateContent(requestMessage);
        }
    }*/

    /*@Override
    public void delete(String content) {
        Message foundMessage = findByContent(content);
        if (foundMessage != null) {
            data.remove(foundMessage);
        }
    }*/
}
