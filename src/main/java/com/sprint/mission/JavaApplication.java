package com.sprint.mission;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;

import java.util.Scanner;

public class JavaApplication {
    static User setupUser(UserService userService, Scanner scanner) { //사용자 생성 테스트 메서드
        System.out.print("사용자명 입력(예시: woody): ");
        String username = scanner.nextLine();

        System.out.print("이메일 입력(예시: woody@codeit.com): ");
        String email = scanner.nextLine();

        System.out.print("비밀번호 입력(예시: woody1234): ");
        String password = scanner.nextLine();

        User user = userService.create(username, email, password);
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
        Channel channel = channelService.create(type, name, description); //채널 생성
        return channel;
    }

    static void messageCreateTest(MessageService messageService, Channel channel, User author, Scanner scanner) { //메시지 생성 테스트
        System.out.print("메시지 입력: ");
        String content = scanner.nextLine();
        Message message = messageService.create(content, channel.getId(), author.getId()); //메시지 생성
        System.out.println("메시지 생성: " + message.getId()); //생성된 메시지 ID 출력
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); //콘솔 입력 객체 생성
        //레포지토리 초기화
        UserRepository userRepository = new FileUserRepository();
        ChannelRepository channelRepository = new FileChannelRepository();
        MessageRepository messageRepository = new FileMessageRepository();

        //서비스 초기화
        UserService userService = new BasicUserService(userRepository);
        ChannelService channelService = new BasicChannelService(channelRepository);
        MessageService messageService = new BasicMessageService(messageRepository);

        //셋업
        User usersetup = setupUser(userService, scanner);
        Channel channelsetup = setupChannel(channelService, scanner);

        //테스트
        messageCreateTest(messageService, channelsetup, usersetup, scanner);
    }
}