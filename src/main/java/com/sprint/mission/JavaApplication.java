package com.sprint.mission;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.repository.file.*;
import com.sprint.mission.discodeit.service.*;
import com.sprint.mission.discodeit.service.basic.*;
import com.sprint.mission.discodeit.service.file.*;

import java.nio.file.*;
import java.util.*;

public class JavaApplication {

    public static void main(String[] args) {

        UserRepository userRepository =
                new FileUserRepository(Paths.get("data/users.ser"));

        ChannelRepository channelRepository =
                new FileChannelRepository(Paths.get("data/channels.ser"));

        MessageRepository messageRepository =
                new FileMessageRepository(Paths.get("data/messages.ser"));

        UserService userService =
                new BasicUserService(userRepository);

        ChannelService channelService =
                new BasicChannelService(channelRepository);

        MessageService messageService =
                new BasicMessageService(
                        messageRepository,
                        userService,
                        channelService
                );
        printFileStatus();

        User user1 = runUserScenario(userService);
        Channel channel1 = runChannelScenario(channelService);

        Message message1 = runMessageScenario(
                messageService,
                user1,
                channel1
        );

        User user2 = userService.create("강동원", "dongwontuna1982@yuha.com", "KDW810118");
        Channel channel2 = channelService.create("Python", "Python 같이 공부해요", ChannelType.PRIVATE);
        Message message2 = messageService.create(
                "잘부탁드립니다.",
                channel2.getId(),
                user2.getId()
        );

        printAllData(userService, channelService, messageService);

        runDeleteScenario(
                userService,
                channelService,
                messageService,
                user2,
                channel2,
                message2
        );

        System.out.println();
        System.out.println("=== 삭제 후 조회 ===");
        printAllData(userService, channelService, messageService);
    }

    private static void printFileStatus() {
        System.out.println("현재 실행 위치: " + Paths.get("").toAbsolutePath());
        System.out.println("users.ser 위치: " + Paths.get("data/users.ser").toAbsolutePath());

        System.out.println("users.ser = " + Files.exists(Paths.get("data/users.ser")));
        System.out.println("channels.ser = " + Files.exists(Paths.get("data/channels.ser")));
        System.out.println("messages.ser = " + Files.exists(Paths.get("data/messages.ser")));
    }

    private static User runUserScenario(UserService userService) {
        System.out.println();
        System.out.println("=== User 테스트 ===");

        User user = userService.create("신혜선", "shinhyesun@yuha.com", "abcd1234");

        System.out.println("생성 후 조회:");
        System.out.println(userService.read(user.getId()));

        user.updateEmail("SYS1234@yuha.com");
        userService.update(user.getId(), user.getUserName(), user.getEmail(), user.getPassWord());

        System.out.println("수정 후 조회:");
        System.out.println(userService.read(user.getId()));

        return user;
    }

    private static Channel runChannelScenario(ChannelService channelService) {
        System.out.println();
        System.out.println("=== Channel 테스트 ===");

        Channel channel = channelService.create(
                "JavaSpring",
                "같이 공부해요",
                ChannelType.PUBLIC
        );


        System.out.println("생성 후 조회:");
        System.out.println(channelService.read(channel.getId()));

        return channel;
    }

    private static Message runMessageScenario(
            MessageService messageService,
            User user,
            Channel channel
    ) {
        System.out.println();
        System.out.println("=== Message 테스트 ===");

        Message message = messageService.create(
                "안녕하세요",
                channel.getId(),
                user.getId()
        );

        System.out.println("생성 후 조회:");
        System.out.println(messageService.read(message.getId()));

        messageService.update(message.getId(), "수정된 메시지입니다.");

        System.out.println("수정 후 조회:");
        System.out.println(messageService.read(message.getId()));

        return message;
    }

    private static void runDeleteScenario(
            UserService userService,
            ChannelService channelService,
            MessageService messageService,
            User user,
            Channel channel,
            Message message
    ) {
        System.out.println();
        System.out.println("=== 삭제 테스트 ===");

        System.out.println("삭제 유저: " + userService.read(user.getId()));
        System.out.println("삭제 채널: " + channelService.read(channel.getId()));
        System.out.println("삭제 메시지: " + messageService.read(message.getId()));

        messageService.delete(message.getId());
        channelService.delete(channel.getId());
        userService.delete(user.getId());
    }

    private static void printAllData(
            UserService userService,
            ChannelService channelService,
            MessageService messageService
    ) {
        System.out.println();
        System.out.println("=== 전체 조회 ===");

        System.out.println("=== 유저 정보 ===");
        userService.readAll().forEach(System.out::println);

        System.out.println("=== 채널 정보 ===");
        channelService.readAll().forEach(System.out::println);

        System.out.println("=== 메세지 정보 ===");
        messageService.readAll().forEach(System.out::println);
    }
}