package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.ArrayList;
import java.util.List;

public class JCFMessageRepository implements MessageRepository {

    // JCF(Java Collection Framework) 리스트로 메모리 저장소 구현
    private final List<Message> messages = new ArrayList<>();

    @Override
    public Message create(Message message) {
        if(message == null || message.getContent() == null ||
                "".equals(message.getContent().trim()) || message.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("메세지 내용은 비어있을 수 없습니다.");
        }
        messages.add(message);
        return message;
    }

    @Override
    public Message findByContent(String content) {
        if (content == null || "".equals(content.trim()) || content.trim().isEmpty()) {
            return null;
        }
        return messages.stream()
                .filter(m -> content.equals(m.getContent()))
                .findFirst().orElse(null);
    }

    @Override
    public List<Message> findAll() {
        return new ArrayList<>(messages);
    }

    @Override
    public void update(Message requestMessage) {
        for (Message message : messages) {
            if (message.getContent().equals(requestMessage.getContent())) {
                message.updateContent(requestMessage);
                break;
            }
        }
    }

    @Override
    public void delete(String content) {
        messages.removeIf(m -> m.getContent().equals(content));
    }
}
/*
레포지토리 설계 및 구현
[ ] 다음의 조건을 만족하는 레포지토리 인터페이스의 구현체를 작성하세요.
[ ] 기존에 구현한 JCF*Service 구현체의 "저장 로직"과 관련된 코드를 참고하여 구현하세요.
 */