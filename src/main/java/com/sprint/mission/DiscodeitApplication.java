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

@ConfigurationPropertiesScan //configurationProperties가 붙은 설정 클래스들을 자동으로 스캔
@SpringBootApplication
public class DiscodeitApplication { //spring boot 메인 애플리케이션 클래스

    static User setupUser(UserService userService, Scanner scanner) { //사용자 생성 테스트 메서드
        System.out.print("사용자명 입력(예시: woody): ");
        String username = scanner.nextLine();

        System.out.print("이메일 입력(예시: woody@codeit.com): ");
        String email = scanner.nextLine();

        System.out.print("비밀번호 입력(예시: woody1234): ");
        String password = scanner.nextLine();

        UserCreateRequest request = new UserCreateRequest(username, email, password); //사용자 생성 요청 DTO 생성
        User user = userService.create(request,Optional.empty()); //프로필 이미지 없이 사용자 생성
        return user; //생성된 사용자 반환
    }

    static Channel setupChannel(ChannelService channelService, Scanner scanner) { //채널 생성 테스트 메서드
        System.out.print("채널 타입(PUBLIC/PRIVATE): ");
        ChannelType type = ChannelType.valueOf( //입력값을 대문자로 변환 후 Enum으로 변환
                scanner.nextLine().toUpperCase());

        System.out.print("채널명 입력(예시: 공지): ");
        String name = scanner.nextLine();

        System.out.print("채널 설명 입력(예시: 공지채널입니다.): ");
        String description = scanner.nextLine();

        PublicChannelCreateRequest request = new PublicChannelCreateRequest(type, name, description); //채널 생성 요청 DTO 생성
        Channel channel = channelService.create(request); //채널 생성
        return channel; //생성된 채널 반환
    }

    static void messageCreateTest(MessageService messageService, Channel channel, User author, Scanner scanner) { //메시지 생성 테스트
        System.out.print("메시지 입력: ");
        String content = scanner.nextLine();
        MessageCreateRequest request = new MessageCreateRequest(content, channel.getId(), author.getId()); //메시지 생성 요청 DTO 생성
        Message message = messageService.create(request, new ArrayList<>()); //첨부 파일 없이 메시지생성 (ArrayList<>() 첨부파일 목록)
        System.out.println("메시지 생성: " + message.getId()); //생성된 메시지 ID 출력
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

        Scanner scanner = new Scanner(System.in); //콘솔 입력 객체 생성
        //서비스 초기화
        UserService userService = context.getBean(UserService.class); //userService Bean 조회
        ChannelService channelService = context.getBean(ChannelService.class); //channelService Bean 조회
        MessageService messageService = context.getBean(MessageService.class); //messageService Bean 조회

        //셋업
        User usersetup = setupUser(userService, scanner); //사용자 생성
        Channel channelsetup = setupChannel(channelService, scanner); //채널 생성

        //테스트
        messageCreateTest(messageService, channelsetup, usersetup, scanner); //메시지 생성
    }

}
