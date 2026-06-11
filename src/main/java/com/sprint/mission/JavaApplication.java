package com.sprint.mission;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

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
            System.out.println("유저 생성: "+user.getId());

            //조회
            User foundUser = userService.find(user.getId());
            System.out.println("유저 조회(단건): " + foundUser.getId());
            List<User> foundUsers = userService.findAll();
            System.out.println("유저 조회(다건): " + foundUsers.size());

            //수정(Update)
            System.out.print("새 비밀번호: ");
            String newPassword = scanner.nextLine();
            User updatedUser = userService.update(user.getId(),null, null, newPassword);
            System.out.println("유저 수정: "+String.join("/", updatedUser.getName(),updatedUser.getEmail(), updatedUser.getPassword()));

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
            System.out.println("채널 생성: "+channel.getId());

            //조회
            Channel foundChannel = channelService.find(channel.getId());
            System.out.println("채널 조회(단건): " + foundChannel.getId());
            List<Channel> foundChannels = channelService.findAll();
            System.out.println("채널 조회(다건): " + foundChannels.size());

            //수정(Update)
            System.out.print("수정할 채널명 입력(예시: 공지사항): ");
            String newName = scanner.nextLine();
            Channel updatedChannel = channelService.update(channel.getId(), newName, null);
            System.out.println("채널 수정: "+String.join("/", updatedChannel.getName(),updatedChannel.getDescription()));

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
        static void messageCRUDTest(MessageService messageService,Channel channel, User user, Scanner scanner) {
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
            UserService userService = new JCFUserService();
            ChannelService channelService = new JCFChannelService();
            MessageService messageService = new JCFMessageService(channelService, userService);

            //테스트
            userCRUDTest(userService, scanner);
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
            messageCRUDTest(messageService, channel, user, scanner);

            /*//셋업
            User usersetup = setupUser(userService);
            Channel channelsetup = setupChannel(channelService);

            //테스트
            messageCreateTest(messageService, channelsetup, usersetup);*/
        }

    /*
     static User setupUser(UserService userService) {

        User user = userService.create("woody","woody@codeit.com","woody1234");
        return user;
    }

    static Channel setupChannel(ChannelService channelService) {
        Channel channel = channelService.create(ChannelType.PUBLIC, "공지","공지채널입니다.");
        return channel;
    }

    static void MessageCreateTest(MessageService messageService, Channel channel, User userId) {
        Message message = messageService.create("안녕하세요.",channel.getId(), userId.getId());
        System.out.println("메시지 생성: " + message.getId());
    }

    public static void main(String[] args) {
        // 1. 각 도메인별 서비스 구현체 준비
        UserRepository userRepository = new FileUserRepository();
        ChannelRepository channelRepository = new FileChannelRepository();
        MessageRepository messageRepository = new FileMessageRepository();

        UserService userService = new BasicUserService(UserRepository);
        ChannelService channelService = new BasicChannelService(channelRepository);
        MessageService messageService = new BasicMessageService(messageRepository, channelRepository, userRepository);

        Scanner scanner = new Scanner(System.in); //입력을 받기 위한 Scanner 객체 생성

        User user = setupUser(userService);
        Channel channel = setupChannel(channelService);

        MessageCreateTest(messageService, channel, user);

        *
        //User 테스트

        //등록(Create)
        System.out.print("등록할 사용자 이름을 입력하세요: "); //사용자에게 이름 입력을 요청하는 메시지 출력
        String userName = scanner.nextLine(); //사용자가 입력한 이름을 읽어서 userName 변수에 저장
        User user = new User(userName); //입력받은 이름으로 User 객체 생성
        userService.create(user); //생성한 User 객체를 userService의 create 메소드로 등록

        //단건조회(Read)
        User foundUser = userService.read(user.getId()); //id로 한 명 찾기
        System.out.println("조회한 사용자 이름: " + foundUser.getName()); //결과 출력

        //전체조회(ReadAll)
        List<User> users = userService.readAll(); //전체 리스트 가져오기
        System.out.println("전체 사용자 수: " + users.size());

        //수정(Update)
        System.out.print("수정할 사용자 이름을 적어주세요: "); //변경할 사용자 이름 입력
        String newUserName = scanner.nextLine();
        user.updateName(newUserName); //User 객체 내부이름변경
        userService.update(user); //변경 정보를 서비스에 반영

        //수정된 데이터 조회
        User updatedUser = userService.read(user.getId());// 수정된 사용자 정보를 다시 조회하여 변경 사항이 정상적으로 저장되었는지 확인
        System.out.println("수정 후 사용자 이름: " + updatedUser.getName());

        //삭제(Delete)
        userService.delete(user.getId()); //사용자 삭제

        //삭제 후 조회(존재하는지 확인)
        User deletedUser = userService.read(user.getId());
        if (deletedUser == null) { // 삭제 후 다시 조회하여 실제로 삭제되었는지 확인
            System.out.println("사용자가 성공적으로 삭제되었습니다.");
        }


        // CHannel테스트
        //등록
        System.out.print("등록할 채널명을 입력하세요: ");
        String channelName = scanner.nextLine();
        Channel channel = new Channel(channelName);
        channelService.create(channel);

        //단건조회
        Channel foundChannel = channelService.read(channel.getId());
        System.out.println("조회한 채널명: " + foundChannel.getName());

        //전체조회
        List<Channel> channels = channelService.readAll();
        System.out.println("전체 채널 수: " + channels.size());

        //수정
        System.out.print("수정할 채널명을 적어주세요: ");
        String newChannelName = scanner.nextLine();
        channel.updateName(newChannelName);
        channelService.update(channel);

        //수정된 데이터 조회
        Channel updatedchannel = channelService.read(channel.getId());
        System.out.println("수정 후 채널 이름: " + updatedchannel.getName());

        //삭제
        channelService.delete(channel.getId());

        //삭제 후 조회
        Channel deletedChannel = channelService.read(channel.getId());
        if (deletedChannel == null) {
            System.out.println("채널이 성공적으로 삭제되었습니다.");
        }


        //Message테스트
        //등록
        System.out.print("메세지 보낼 사용자이름을 입력해주세요: ");
        String messageUserName = scanner.nextLine();
        User messageUser = new User(messageUserName);
        userService.create(messageUser);

        System.out.print("메세지 보낼 채널명을 입력해주세요: ");
        String messageChannelName = scanner.nextLine();
        Channel messageChannel = new Channel(messageChannelName);
        channelService.create(messageChannel);

        System.out.print("등록할 메시지 내용을 입력해주세요: ");
        String messageContent = scanner.nextLine();
        Message message = new Message(messageContent, messageUser.getId(), messageChannel.getId());
        messageService.create(message);

        //단건조회
        Message foundMessage = messageService.read(message.getId());
        System.out.println("조회한 메세지 이름: " + foundMessage.getContent());

        //전체조회
        List<Message> messages = messageService.readAll();
        System.out.println("전체 메세지 수: " + messages.size());

        //수정
        System.out.print("수정할 메세지 내용을 적어주세요: ");
        String newMessageContent = scanner.nextLine();
        message.updateContent(newMessageContent);
        messageService.update(message);

        //수정된 데이터 조회
        Message updatedMessage = messageService.read(message.getId());
        System.out.println("수정 후 메세지 이름: " + updatedMessage.getContent());

        //삭제
        messageService.delete(message.getId());

        //삭제 후 조회
        Message deletedMessage = messageService.read(message.getId());
        if (deletedMessage == null) {
            System.out.println("메세지가 성공적으로 삭제되었습니다.");
        } *

    }*/

}