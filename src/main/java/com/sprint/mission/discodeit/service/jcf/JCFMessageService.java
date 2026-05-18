package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.ArrayList;
import java.util.List;

public class JCFMessageService implements MessageService {

    private final List<Message> data;

    public JCFMessageService() {
        this.data = new ArrayList<>();
    }

    public Message create(Message message) { // 매개변수 선언, 유저 생성하는 기능 구현
        data.add(message); // 창고에 넣기 (진짜 등록)
        return message;
    }

    public Message findByContent(String content) { // 단건 조회
        for (Message foundMessage : data) {
            if (foundMessage.getContent().equals(content)) {
                return foundMessage;
            }
        }
        return null;
    }

    public List<Message> findAll() { // 전체 조회
        return data;
    }

    public void update(Message requestMessage) {
        Message foundMessage = findByContent(requestMessage.getContent());
        if (foundMessage != null) {
            foundMessage.updateContent(requestMessage);
        }
    }

    public void delete(String content) {
        Message foundMessage = findByContent(content);
        if (foundMessage != null) {
            data.remove(foundMessage);
        }
    }
}
