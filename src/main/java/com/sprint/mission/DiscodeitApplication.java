package com.sprint.mission;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

@ConfigurationPropertiesScan
@SpringBootApplication
public class DiscodeitApplication {

    static User setupUser(UserService userService, Scanner scanner) { //사용자 생성 테스트 메서드
        System.out.print("사용자명 입력(예시: woody): ");
        String username = scanner.nextLine();

        System.out.print("이메일 입력(예시: woody@codeit.com): ");
        String email = scanner.nextLine();

        System.out.print("비밀번호 입력(예시: woody1234): ");
        String password = scanner.nextLine();

        UserCreateRequest request = new UserCreateRequest(username, email, password);
        User user = userService.create(request,Optional.empty());
        return user;
    }

    static Channel setupChannel(ChannelService channelService, Scanner scanner) { //채널 생성 테스트 메서드
        System.out.print("채널 타입(PUBLIC/PRIVATE): ");
        ChannelType type = ChannelType.valueOf( //입력값을 대문자로 변환 후 Enum으로 변환
                scanner.nextLine().toUpperCase());

        System.out.print("채널명 입력(예시: 공지): ");
        String name = scanner.nextLine();

        System.out.print("채널 설명 입력(예시: 공지채널입니다.): ");
        String description = scanner.nextLine();

        PublicChannelCreateRequest request = new PublicChannelCreateRequest(type, name, description);
        Channel channel = channelService.create(request); //채널 생성
        return channel;
    }

    static void messageCreateTest(MessageService messageService, Channel channel, User author, Scanner scanner) { //메시지 생성 테스트
        System.out.print("메시지 입력: ");
        String content = scanner.nextLine();
        MessageCreateRequest request = new MessageCreateRequest(content, channel.getId(), author.getId());
        Message message = messageService.create(request, new ArrayList<>()); //메시지 생성
        System.out.println("메시지 생성: " + message.getId()); //생성된 메시지 ID 출력


    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

        Scanner scanner = new Scanner(System.in); //콘솔 입력 객체 생성
        //서비스 초기화
        UserService userService = context.getBean(UserService.class);
        ChannelService channelService = context.getBean(ChannelService.class);
        MessageService messageService = context.getBean(MessageService.class);

        //셋업
        User usersetup = setupUser(userService, scanner);
        Channel channelsetup = setupChannel(channelService, scanner);

        //테스트
        messageCreateTest(messageService, channelsetup, usersetup, scanner);
    }

}
