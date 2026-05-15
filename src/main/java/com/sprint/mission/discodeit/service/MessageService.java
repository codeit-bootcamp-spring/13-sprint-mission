package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

public interface MessageService {

    Message createMessage(User user, Channel channel, String message);
    void printMessage(Message message);
    void editMessage(Message message, User user, String newMessage);
    Message deleteMessage(Message message, User user);

    void printWriter(Message message);

    void printChannel(Message message);


    /*
- message 관련
    - [ ]  메세지 생성 메서드
    - [ ]  해당 메세지 정보 출력 메서드
    - [ ]  해당 메세지 수정 메서드
    - [ ]  해당 메세지 삭제 메서드
- user 관련
    - [ ]  해당 메세지의 작성자 읽기(출력) 메서드
- channel 관련
    - [ ]  해당 메세지가 작성된 채널 정보 읽기(출력) 메서드
     */
}
