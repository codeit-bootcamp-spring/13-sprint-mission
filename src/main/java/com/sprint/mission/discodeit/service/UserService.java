package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

public interface UserService {

    User createUser(String name, String email);
    void printUserInfo(User user);
    void changeName(User user, String newName);
    void changeEmail(User user, String newEmail);
    User deleteUser(User user);

    //Channel createChannel(User user, String channelName);
    void joinChannel(User user, Channel channel);
    void printMyChannelsInfo(User user);
    void leaveChannel(User user, Channel channel);

    //Message createMessage(User user, Channel channel, String message);
    void printMessages(User user);
    //void editMessage(User user, Message message, String newMessage);
    //Message deleteMessage(User user, Message message);

    /*
- user 관련
    - [ ]  유저 생성 메서드
    - [ ]  정보 출력 메서드
    - [ ]  이름 변경 메서드
    - [ ]  이메일 변경 메서드
    - [ ]  유저 삭제 메서드
- channels 관련
    - [ ]  채널 생성 메서드
        - 생성 후 자동적으로 가입이 되도록
    - [ ]  채널 가입 메서드
    - [ ]  가입한 채널 읽기(출력) 메서드
    - [ ]  채널 탈퇴 메서드
- messages 관련
    - [ ]  메세지 생성 메서드
    - [ ]  자신이 작성한 메세지 읽기(출력) 메서드
        - 채널명, 유저이름, 메세지 형태
    - [ ]  메세지 수정 메서드
    - [ ]  메세지 삭제 메서드
        - 해당 메세지를 작성한 유저만의 권한
- 후순위:
    - Friends 관련
        - [ ]  생성, 읽기, 수정, 삭제 메서드?
     */
}
