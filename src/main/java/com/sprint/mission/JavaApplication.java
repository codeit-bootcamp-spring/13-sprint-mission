package com.sprint.mission;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.repository.file.*;
import com.sprint.mission.discodeit.service.*;
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
                new FileUserService(userRepository);

        ChannelService channelService =
                new FileChannelService(channelRepository);

        MessageService messageService =
                new FileMessageService(messageRepository);

        System.out.println("현재 실행 위치: " + Paths.get("").toAbsolutePath());
        System.out.println("users.ser 위치: " + Paths.get("data/users.ser").toAbsolutePath());

        System.out.println("users.ser = " + Files.exists(Paths.get("data/users.ser")));
        System.out.println("channels.ser = " + Files.exists(Paths.get("data/channels.ser")));
        System.out.println("messages.ser = " + Files.exists(Paths.get("data/messages.ser")));

        User user1 = new User("신혜선", "shinhyesun@yuha.com", "abcd1234");
        Channel channel1 = new Channel("JavaSpring", "같이 공부해요", ChannelType.PUBLIC);
        Message message1 = new Message("안녕하세요", user1.getId(), channel1.getId());

        User user2 = new User("강동원", "dongwontuna1982@yuha.com", "KDW810118");
        Channel channel2 = new Channel("Python", "Python 같이 공부해요", ChannelType.PRIVATE);
        Message message2 = new Message("잘부탁드립니다.", user2.getId(), channel2.getId());

        userService.create(user1);
        userService.create(user2);

        channelService.create(channel1);
        channelService.create(channel2);

        messageService.create(message1);
        messageService.create(message2);

        System.out.println();
        System.out.println("=== 단건 조회 ===");

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
        userService.update(findUser.getId(), findUser);

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

        System.out.println("=== 유저 정보 ===");
        userService.readAll().forEach(System.out::println);

        System.out.println("=== 채널 정보 ===");
        channelService.readAll().forEach(System.out::println);

        System.out.println("=== 메세지 정보 ===");
        messageService.readAll().forEach(System.out::println);
    }
}