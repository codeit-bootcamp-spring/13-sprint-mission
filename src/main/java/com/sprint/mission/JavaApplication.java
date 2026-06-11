package com.sprint.mission;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.file.FileChannelService;
import com.sprint.mission.discodeit.service.file.FileMessageService;
import com.sprint.mission.discodeit.service.file.FileUserService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class JavaApplication {
    static void userCRUDTest(UserService userService, Scanner scanner) {
        System.out.println("==유저 생성 ==");
        System.out.print("사용자 이름 입력(예시: woody): ");
        String username = scanner.nextLine();
        System.out.print("이메일 입력(예시: woody@codeit.com): ");
        String email = scanner.nextLine();
        System.out.print("비밀번호 입력(예시: woody1234): ");
        String password = scanner.nextLine();

        //생성
        User user = userService.create(username, email, password);
        System.out.println("유저 생성: " + user.getId());

        //조회
        User foundUser = userService.find(user.getId());
        System.out.println("유저 조회(단건): " + foundUser.getId());
        List<User> foundUsers = userService.findAll();
        System.out.println("유저 조회(다건): " + foundUsers.size());

        //수정(Update)
        System.out.print("새 비밀번호: ");
        String newPassword = scanner.nextLine();
        User updatedUser = userService.update(user.getId(), null, null, newPassword);
        System.out.println("유저 수정: " + String.join("/", updatedUser.getName(), updatedUser.getEmail(), updatedUser.getPassword()));

        //삭제(Delete)
        userService.delete(user.getId());
        List<User> foundUsersAfterDelete = userService.findAll();
        System.out.println("유저 삭제: " + foundUsersAfterDelete.size());

        //삭제 후 조회(존재하는지 확인)
        try {
            userService.find(user.getId());
            System.out.println("삭제 실패");
        } catch (NoSuchElementException e) {
            System.out.println("삭제 성공");
        }
    }

    static void channelCRUDTest(ChannelService channelService, Scanner scanner) {
        //생성
        System.out.print("채널 타입(PUBLIC/PRIVATE): ");
        ChannelType type = ChannelType.valueOf(
                scanner.nextLine().toUpperCase());
        System.out.print("채널 이름 입력(예시: 공지): ");
        String name = scanner.nextLine();
        System.out.print("채널 설명 입력(예시: 공지채널입니다.): ");
        String decription = scanner.nextLine();

        Channel channel = channelService.create(type, name, decription);
        System.out.println("채널 생성: " + channel.getId());

        //조회
        Channel foundChannel = channelService.find(channel.getId());
        System.out.println("채널 조회(단건): " + foundChannel.getId());
        List<Channel> foundChannels = channelService.findAll();
        System.out.println("채널 조회(다건): " + foundChannels.size());

        //수정(Update)
        System.out.print("수정할 채널명 입력(예시: 공지사항): ");
        String newName = scanner.nextLine();
        Channel updatedChannel = channelService.update(channel.getId(), newName, null);
        System.out.println("채널 수정: " + String.join("/", updatedChannel.getName(), updatedChannel.getDescription()));

        //삭제(Delete)
        channelService.delete(channel.getId());
        List<Channel> foundChannelsAfterDelete = channelService.findAll();
        System.out.println("채널 삭제: " + foundChannelsAfterDelete.size());

        //삭제 후 조회(존재하는지 확인)
        try {
            channelService.find(channel.getId());
            System.out.println("삭제 실패");
        } catch (NoSuchElementException e) {
            System.out.println("삭제 성공");
        }
    }

    static void messageCRUDTest(MessageService messageService, Channel channel, User user, Scanner scanner) {
        //생성
        System.out.print("메시지 입력: ");
        String content = scanner.nextLine();
        Message message = messageService.create(content, channel.getId(), user.getId());
        //Message message = messageService.create(content, channelId, authorId);
        System.out.println("메시지 생성: " + message.getId());

        //조회
        Message foundMessage = messageService.find(message.getId());
        System.out.println("메시지 조회(단건): " + foundMessage.getId());
        List<Message> foundMessages = messageService.findAll();
        System.out.println("메시지 조회(다건): " + foundMessages.size());

        //수정(Update)
        System.out.print("수정할 메시지 입력: ");
        String updateContent = scanner.nextLine();
        Message updatedMessage = messageService.update(message.getId(), updateContent);
        System.out.println("메시지 수정: " + updatedMessage.getContent());
        //삭제(Delete)
        messageService.delete(message.getId());
        List<Message> foundMessagesAfterDelete = messageService.findAll();
        System.out.println("메시지 삭제: " + foundMessagesAfterDelete.size());

        //삭제 후 조회(존재하는지 확인)
        try {
            messageService.find(message.getId());
            System.out.println("삭제 실패");
        } catch (NoSuchElementException e) {
            System.out.print("삭제 성공");
        }
    }

    static User setupUser(UserService userService) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("사용자명 입력(예시: woody): ");
        String username = scanner.nextLine();

        System.out.print("이메일 입력(예시: woody@codeit.com): ");
        String email = scanner.nextLine();

        System.out.print("비밀번호 입력(예시: woody1234): ");
        String password = scanner.nextLine();

        User user = userService.create(username, email, password);
        return user;
    }

    static Channel setupChannel(ChannelService channelService) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("채널 타입(PUBLIC/PRIVATE): ");
        ChannelType type = ChannelType.valueOf(
                scanner.nextLine().toUpperCase());

        System.out.print("채널명 입력(예시: 공지): ");
        String name = scanner.nextLine();

        System.out.print("채널 설명 입력(예시: 공지채널입니다.): ");
        String description = scanner.nextLine();
        Channel channel = channelService.create(type, name, description);
        return channel;
    }

    static void messageCreateTest(MessageService messageService, Channel channel, User author) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("메시지 입력: ");
        String content = scanner.nextLine();
        Message message = messageService.create(content, channel.getId(), author.getId());
        System.out.println("메시지 생성: " + message.getId());
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); //입력을 받기 위한 Scanner 객체 생성
        //서비스 초기화
        UserService userService = new FileUserService();
        ChannelService channelService = new FileChannelService();
        MessageService messageService = new FileMessageService(channelService, userService);

        //테스트
            /*userCRUDTest(userService, scanner);
            channelCRUDTest(channelService, scanner);

            User user =
                    userService.create(
                            "test",
                            "test@test.com",
                            "1234");

            Channel channel =
                    channelService.create(
                            ChannelType.PUBLIC,
                            "general",
                            "테스트");
            messageCRUDTest(messageService, channel, user, scanner);*/

        //셋업
        User usersetup = setupUser(userService);
        Channel channelsetup = setupChannel(channelService);

        //테스트
        messageCreateTest(messageService, channelsetup, usersetup);
    }
}