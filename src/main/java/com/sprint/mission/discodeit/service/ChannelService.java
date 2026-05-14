package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

public interface ChannelService {

    Channel createChannel(String name, User channelHost);
    void printChannelInformation(Channel channel);
    void editChannelName(Channel channel, String newName);
    void deleteChannel(Channel channel, User user);

    void addUserToChannel(Channel channel, User user);
    void printUsersInfo(Channel channel);
    void printChannelHostInfo(Channel channel);
    void changeChannelHost(Channel channel, User user);
    void deleteUserFromChannel(Channel channel, User user);

    Message createMessage(Channel channel, User user, String message);
    void printMessages(Channel channel);
    void editMessage(User user, Message message, String newMessage);
    void deleteMessage(User user, Message message);

    /*
- channel 관련
    - [ ]  채널 생성 메서드
    - [ ]  채널 정보 읽기 메서드
    - [ ]  채널 이름 수정 메서드
    - [ ]  채널 삭제 메서드
- users 관련
    - [ ]  유저 추가 메서드
    - [ ]  유저 정보 읽기(출력) 메서드
        - 해당 채널에 가입한 유저들의 정보
    - [ ]  채널 호스트 읽기(출력) 메서드
    - [ ]  채널 호스트 변경 메서드
    - [ ]  유저 탈퇴 시키는 메서드
        - 해당 채널을 생성한 유저만의 권한
- messages 관련
    - [ ]  메세지 생성 메서드
        - 메세지 작성 유저 정보 포함
    - [ ]  해당 채널에 존재하는 메세지 읽기(출력) 메서드
    - [ ]  메세지 수정 메서드
        - 해당 메세지를 작성한 유저만의 권한
    - [ ]  메세지 삭제 메서드
        - 해당 메세지를 작성한 유저만의 권한
     */
}
