package com.sprint.mission.discodeit;


import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import java.util.List;

public class JavaApplication {
    public static void main(String[] args) {
        //객체를 생성하고 참조 변수에 객체의 주소 값을 할당하는 곳
        //생성자를 호출해서 필드 변수에 값을 할당 받는다.
        UserService userService = new JCFUserService();

        //동일하게 생성자를 호출해서 필드 변수에 값릏 할당 받는다.
        //JCFChannelService안에 생성자를 호출해서 userService 구현 객체를 담는 참조 변수를 할당 받는다.
        ChannelService channelService = new JCFChannelService(userService);

        //동일하게 생성자를 호출해서 필드 변수에 값릏 할당 받는다.
        //JCFMessageService안에 생성자를 호출해서 userService와 channelService 구현 객체를 담는 참조 변수를 할당 받는다.
        MessageService messageService = new JCFMessageService(userService, channelService);


        //객체가 생성 객체 안에는 클레스에 있는 필드변수가 존재한다.
        //생성자가 호출되고 객체의 인수를 순서대로 필드 변수에 할당한다.
        User user = new User("익명이", "1234", "Email");
        userService.create(user);//JCFUserService의 create메서드 호출.->
                                 // List에 user객체주소 추가 -> 여기로 복귀 후 아래로 진행

        //위와 동일한 경로
        User user2 = new User("익명이2", "4321", "이메일");
        userService.create(user2);// 동일 경로

        //위와 동일 경로
        Channel channel = new Channel("임시채", "임시 채널 입니다.");
        channelService.create(channel);// 동일

        //동일 경로
        //단, Message 객체의 인수에는 UserId와 ChannelId를  각각 get()으로 꺼내서 인수로 할당.
        Message message = new Message("안녕하세요", user.getId(), channel.getId());
        messageService.create(message);//동일

        System.out.println("=============단건 조화============");

        //User클레스에 있는 getId호출->id를 가져옴
        // -> JCFUserService의 read메서드 호출 id 할당
        //-> read에서 반환한 객체의주소를 가지고 복귀 -> foundUser에 객체 할당
        User foundUser = userService.read(user.getId());
        //둘다 동일한 경로.
        Channel foundChannel = channelService.read(channel.getId());
        Message foundMessage = messageService.read(message.getId());

        //foundUser변수안에서 getname으로 필드 변수의 값을 가져온다.
        System.out.println(foundUser.getUserName());
        //둘다 동일
        System.out.println(foundChannel.getChannelName());
        System.out.println(foundMessage.getMessage());

        System.out.println("=============다건 조화============");

        //List<User> 타입 변수에 readAll로 가져온 같은 타입 리스트 내 모든 객체의 주소를 할당
        List<User> users =  userService.readAll();
        // 둘다 동일
        List<Channel> channels = channelService.readAll();
        List<Message> messages = messageService.readAll();

        for (User userAll : users) {
            //List<User> 타입 변수 User에서 순차적으로 모든 값을 좌항 변수에 할당)
            //getUserName으로 모든 User객체의 필드 변수에서 name만 출력
            System.out.println(userAll.getUserName());
        }

        //둘다 동일
        for (Channel channel1 : channels) {
            System.out.println(channel1.getChannelName());
        }

        for (Message message1 : messages) {
            System.out.println(message1.getMessage());
        }

        System.out.println("=============수정, 수정 조회============");
        //User class의 메서드 getId호출 -> 객체 id 반환 -> 복귀 후 인수 할당
        //JCFUserService의 update메서드 호출-> 할당된 id사용 user객체 반환
        //-> 반환된 객체를 User class의 update로 할당 필드 변수 변경 후 main 복귀.
        userService.update(user.getId(),"안익명이", "Leemeil", "4432");
        //둘다 동일한 경로로.
        channelService.update(channel.getId(),"안임시", "안임시채널입니다.");
        messageService.update(message.getId(),"안안녕하세요");
            //객체의 필드 변수 값을 각getter로 반환해서 출력
            System.out.println(foundUser.getUserName()+" "+foundUser.getEmail()+" "+foundUser.getPassword());
        System.out.println("====삭제====");

        //user class의 get()메서드 호출->getID로 유저 객체의 id를 반환해서
        //JCFUserService의 delete메서드 호출 후 id 할당
        //delete메서드가 list의 객체 주소를 삭제
        //삭제 후 메서드 종료 호출 위치인 main에 복귀.
        userService.delete(user.getId());
        //동일한 경로
        channelService.delete(channel.getId());
        messageService.delete(message.getId());

        //User class의 getId호출 foundUser에 할당 되있는 객체의 id를 반환
        //->JCFUserService의 read메서드 호출-> null반환 메서드 종료
        //호출 위치인 main에 복귀 후 null을 출력한다.
        System.out.println(userService.read(foundUser.getId()));
    }


}
