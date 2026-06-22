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

    // 매개변수 변경으로 인한 오류로 빈 메서드 생성
    @Override
    public Message create(String content, UUID channelId, UUID userId) {
        return new Message(content, channelId, userId);
    }

    // 생성
//    @Override
//    public void create(Message message) {
//        data.add(message);
//    }

    // 조회
    @Override
    public Message find(UUID id) {
        for (Message m : data) {
            if (id.equals(m.getId())) {
                return m;
            }
        }
        throw new IllegalArgumentException("메시지를 찾을 수 없습니다.");
    }

    // 모두 조회
    @Override
    public List<Message> findAll() {
        if (!data.isEmpty()) {
            return data;
        }
        return Collections.emptyList();
    }

    // 수정
    @Override
    public void update(UUID id, String content) {
        for (Message m : data) {
            if (id.equals(m.getId())) {
                m.update(content);
                return;
            }
        }
        throw new IllegalArgumentException("메시지를 찾을 수 없습니다.");
    }

    // 삭제
    @Override
    public void delete(UUID id) {
        for (Message message : data) {
            if (message.getId().equals(id)) {
                data.remove(message);
                return;
            }
        }
        throw new IllegalArgumentException("메시지를 찾을 수 없습니다.");
    }

}
