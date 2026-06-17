package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.service.*;
import com.sprint.mission.discodeit.repository.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.*;

import java.nio.file.*;
import java.util.*;

@SpringBootApplication
public class DiscodeitApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

        UserService userService =
                context.getBean(UserService.class);

        ChannelService channelService =
                context.getBean(ChannelService.class);

        MessageService messageService =
                context.getBean(MessageService.class);


        UserRepository userRepository =
                context.getBean(UserRepository.class);

        ChannelRepository channelRepository =
                context.getBean(ChannelRepository.class);

        MessageRepository messageRepository =
                context.getBean(MessageRepository.class);

        System.out.println("=== 현재 등록된 Repository Bean ===");
        System.out.println("UserRepository = " + userRepository.getClass().getSimpleName());
        System.out.println("ChannelRepository = " + channelRepository.getClass().getSimpleName());
        System.out.println("MessageRepository = " + messageRepository.getClass().getSimpleName());

        printFileStatus();

        UserResponse user1 = runUserScenario(userService);
        ChannelResponse channel1 = runChannelScenario(channelService);
        MessageResponse message1 = runMessageScenario(
                messageService,
                user1,
                channel1
        );

       UserResponse user2 =  userService.create(new UserRequest.CreateUserRequest(
                "강동원",
                "kando99@naver.com",
                "1234zxc",
                null));

        ChannelResponse channel2 =
                channelService.createPrivateChannel(
                        new ChannelRequest.CreatePrivateChannel(
                                List.of(user1.id(), user2.id())
                        )
                );

        MessageResponse message2 = messageService.create(
                new MessageRequest.CreateMessageRequest(
                        user2.id(),
                        channel2.id(),
                        "잘부탁드립니다.",
                        List.of()
                )
        );

        printAllData(userService, channelService, messageService, channel1);

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
        printAllData(userService, channelService, messageService, channel1);
    }

    private static void printFileStatus() {
        System.out.println("현재 실행 위치: " + Paths.get("").toAbsolutePath());

        System.out.println(".discodeit/users.ser 위치: "
                + Paths.get(".discodeit/users.ser").toAbsolutePath());

        System.out.println("users.ser = "
                + Files.exists(Paths.get(".discodeit/users.ser")));

        System.out.println("channels.ser = "
                + Files.exists(Paths.get(".discodeit/channels.ser")));

        System.out.println("messages.ser = "
                + Files.exists(Paths.get(".discodeit/messages.ser")));
    }

    private static UserResponse runUserScenario(UserService userService) {
        System.out.println();
        System.out.println("=== User 테스트 ===");
        System.out.println();

        UserResponse user1 = userService.create(new UserRequest.CreateUserRequest(
                "신혜선",
                "shinhyesun@yuha.com",
                "abcd1234",
                null
        ));

        System.out.println();
        System.out.println("1. 생성 후 조회");
        System.out.println();
        System.out.println(userService.find(user1.id()));

        userService.update(
                user1.id(),
                new UserRequest.UpdateUserRequest(
                        user1.userName(),
                        "SYS1234@yuha.com",
                        "abcd1234",
                        null
                )
        );


        System.out.println("2. 수정 후 조회");
        System.out.println();
        System.out.println(userService.find(user1.id()));

        return user1;
    }

    private static ChannelResponse runChannelScenario(ChannelService channelService) {
        System.out.println();
        System.out.println("=== Channel 테스트 ===");
        System.out.println();

        ChannelResponse channel = channelService.createPublicChannel(
                new ChannelRequest.CreatePublicChannel(
                        "JavaSpring",
                        "같이 공부해요"
                )
        );

        System.out.println("1. 생성 후 조회");
        System.out.println();
        System.out.println(channelService.find(channel.id()));

        return channel;
    }

    private static MessageResponse runMessageScenario(
            MessageService messageService,
            UserResponse user,
            ChannelResponse channel
    ) {
        System.out.println();
        System.out.println("=== Message 테스트 ===");
        System.out.println();

        MessageResponse message = messageService.create(
                new MessageRequest.CreateMessageRequest(
                        user.id(),
                        channel.id(),
                        "안녕하세요",
                        List.of()
                )
        );
        System.out.println("1. 생성 후 조회:");
        System.out.println();
        System.out.println(messageService.find(message.id()));

        messageService.update(
                message.id(),
                new MessageRequest.UpdateMessageRequest(
                        "수정된 메시지입니다.",
                        List.of()
                )
        );

        System.out.println("2. 수정 후 조회:");
        System.out.println();
        System.out.println(messageService.find(message.id()));

        return message;
    }

    private static void runDeleteScenario(
            UserService userService,
            ChannelService channelService,
            MessageService messageService,
            UserResponse user,
            ChannelResponse channel,
            MessageResponse message
    ) {
        System.out.println();
        System.out.println("=== 삭제 테스트 ===");

        System.out.println("삭제 유저: " + userService.find(user.id()));
        System.out.println("삭제 채널: " + channelService.find(channel.id()));
        System.out.println("삭제 메시지: " + messageService.find(message.id()));

        messageService.delete(message.id());
        channelService.delete(channel.id());
        userService.delete(user.id());
    }

    private static void printAllData(
            UserService userService,
            ChannelService channelService,
            MessageService messageService,
            ChannelResponse channel
    ) {
        System.out.println();
        System.out.println("=== 전체 조회 ===");
        System.out.println();

        userService.findAll().forEach(System.out::println);

        channelService.findAll().forEach(System.out::println);

        messageService.findAllByChannelId(channel.id())
                .forEach(System.out::println);
    }
}