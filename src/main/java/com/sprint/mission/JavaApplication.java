package com.sprint.mission;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.service.*;
import com.sprint.mission.discodeit.service.jcf.*;

import java.util.*;

public class JavaApplication {

    public static void main(String[] args) {


        UserService userService = new JCFUserService();
        ChannelService channelService = new JCFChannelService();
        MessageService messageService =
                new JCFMessageService(userService, channelService);

        User user1 = new User("신혜선","shinhyesun@yuha.com","abcd1234");
        Channel channel1 = new Channel("JavaSpring","같이 공부해요", ChannelType.PUBLIC);
        Message message1 = new Message("안녕하세요",user1.getId(), channel1.getId());

        User user2 = new User("강동원","dongwontuna1982@yuha.com","KDW810118" );
        Channel channel2 = new Channel( "Python","Python 같이 공부해요", ChannelType.PRIVATE);
        Message message2 = new Message("잘부탁드립니다.",user2.getId(), channel2.getId());

        List.of(user1, user2).forEach(userService::create);
        List.of(channel1, channel2).forEach(channelService::create);
        List.of(message1, message2).forEach(messageService::create);

        System.out.println("=== 단건 조회 ===");

        try {
            Message errorMessage = new Message(
                    "에러 테스트 메시지",
                    UUID.randomUUID(),
                    UUID.randomUUID()
            );
            messageService.create(errorMessage);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // User 정보 에러 확인
        try {
            new User("","zzz100@yuha.com","1234567");
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }


        System.out.println(userService.read(user1.getId()));
        System.out.println(channelService.read(channel1.getId()));
        System.out.println(messageService.read(message1.getId()));

        System.out.println();
        System.out.println(userService.read(user2.getId()));
        System.out.println(channelService.read(channel2.getId()));
        System.out.println(messageService.read(message2.getId()));

        System.out.println();
        System.out.println("=== 다건 조회 ===");

        System.out.println("=== 유저 정보 ===");
        userService.readAll().forEach(System.out::println);

        System.out.println("=== 채널 정보 ===");
        channelService.readAll().forEach(System.out::println);

        System.out.println("=== 메세지 정보 ===");
        messageService.readAll().forEach(System.out::println);

        System.out.println();
        System.out.println("=== 수정 ===");
        User findUser = userService.read(user1.getId());
        findUser.updateEmail("SYS1234@yuha.com");
        System.out.println(userService.read(user1.getId()));

        System.out.println();
        System.out.println("=== 수정 후 조회 ===");
        System.out.println(userService.read(user1.getId()));

        System.out.println();
        System.out.println("=== 삭제 ===");
        System.out.println("삭제 유저: " + userService.read(user2.getId()));
        System.out.println("삭제 채널: " + channelService.read(channel2.getId()));
        System.out.println("삭제 메시지: " + messageService.read(message2.getId()));

        messageService.delete(message2.getId());
        channelService.delete(channel2.getId());
        userService.delete(user2.getId());

        System.out.println();
        System.out.println("=== 삭제 후 조회 ===");
        System.out.println(userService.readAll());
        System.out.println(channelService.readAll());
        System.out.println(messageService.readAll());

    }

}

