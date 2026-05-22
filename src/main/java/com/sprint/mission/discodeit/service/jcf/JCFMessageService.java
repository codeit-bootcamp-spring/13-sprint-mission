package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class JCFMessageService implements MessageService {

    private final List<Message> data;

    public JCFMessageService(){
        this.data = new ArrayList<>();
    }

    // 생성
    @Override
    public void createMessage(Message message) {
        data.add(message);
    }

    // 조회
    @Override
    public Message findMessage(UUID id) {
        for (Message m : data) {
            if (m.getId().equals(id)) {
                return m;
            }
        }
        throw new IllegalArgumentException("메시지를 찾을 수 없습니다.");
    }

    // 모두 조회
    @Override
    public List<Message> findAllMessages() {
        if (!data.isEmpty()) {
            return data;
        }
        return Collections.emptyList();
    }

    // 수정
    @Override
    public void updateMessage(UUID id, String content) {
        for (Message m : data) {
            if (m.getId().equals(id)) {
                m.update(content);
                return;
            }
        }
        throw new IllegalArgumentException("메시지를 찾을 수 없습니다.");
    }

    // 삭제
    @Override
    public void deleteMessage(UUID id) {
        for (Message message : data) {
            if (message.getId().equals(id)) {
                data.remove(message);
                return;
            }
        }
        throw new IllegalArgumentException("메시지를 찾을 수 없습니다.");
    }

}
