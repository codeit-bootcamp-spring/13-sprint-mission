package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.file.FileChannel;
import com.sprint.mission.discodeit.service.file.FileMessage;
import com.sprint.mission.discodeit.service.file.FileUser;
import com.sprint.mission.discodeit.service.jcf.JCFChannel;
import com.sprint.mission.discodeit.service.jcf.JCFMessage;
import com.sprint.mission.discodeit.service.jcf.JCFUser;

import java.util.List;
import java.util.Scanner;

public class JavaApplication {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        UserService userService = new FileUser();
        ChannelService channelService = new FileChannel();
        MessageService messageService = new FileMessage();

        while (true) {

            System.out.println("===== DISCODEIT =====");
            System.out.println("1. 등록");
            System.out.println("2. 조회");
            System.out.println("3. 수정");
            System.out.println("4. 삭제");
            System.out.println("0. 종료");
            System.out.print("무엇을 실행하시겠습니까?: ");

            int number = scanner.nextInt();
            scanner.nextLine();

            switch (number) {
                case 1:
                    System.out.println("===== 등록 =====");
                    System.out.println("1. 유저 등록");
                    System.out.println("2. 방 등록");
                    System.out.println("3. 메세지 작성");
                    System.out.print("어떤 데이터를 등록하시겠습니까?: " );

                    int createNumber = scanner.nextInt();
                    scanner.nextLine();

                    switch (createNumber) {
                        case 1:
                            System.out.print("유저 이름 입력 : ");

                            String userName = scanner.nextLine();
                            User user = new User(userName);
                            userService.create(user);

                            System.out.println("유저 등록 완료");
                            break;

                        case 2:
                            System.out.print("방 이름 입력 : ");

                            String room = scanner.nextLine();
                            Channel channel = new Channel(room);
                            channelService.create(channel);

                            System.out.println("방 등록 완료");
                            break;

                        case 3:
                            List<Channel> channels = channelService.readAll();

                            if (channels.isEmpty()) {
                                System.out.println("현재 생성된 방이 없습니다.");
                                break;
                            }

                            System.out.println("===== 방 목록 =====");
                            for (int i = 0; i < channels.size(); i++) {
                                System.out.println((i + 1) + ". " + channels.get(i).getRoom());
                            }

                            System.out.print("방 선택 : ");

                            int choice = scanner.nextInt();
                            scanner.nextLine();

                            System.out.print("메세지 입력 : ");
                            String talk = scanner.nextLine();

                            Message message = new Message(talk);
                            messageService.create(message);

                            System.out.println("메세지 등록 완료");
                            break;

                        default:
                            System.out.println("잘못된 번호입니다.");
                            break;
                    }
                    break;

                case 2:
                    System.out.println("===== 조회 =====");
                    System.out.println("1. 전체 조회");
                    System.out.println("2. 유저 조회");
                    System.out.println("3. 방 조회");
                    System.out.println("4. 메세지 조회");
                    System.out.print("어떤 데이터를 조회하시겠습니까?: " );

                    int readNumber = scanner.nextInt();
                    scanner.nextLine();

                    switch (readNumber) {
                        case 1:
                            System.out.println("===== 유저 =====");
                            for (User users : userService.readAll()) {
                                System.out.println(users.getName());
                            }

                            System.out.println("===== 방 =====");
                            for (Channel channels : channelService.readAll()) {
                                System.out.println(channels.getRoom());
                            }

                            System.out.println("===== 메세지 =====");
                            for (Message messages : messageService.readAll()) {
                                System.out.println(messages.getTalk());
                            }
                            break;

                        case 2:
                            System.out.println("===== 유저 =====");
                            for (User users : userService.readAll()) {
                                System.out.println(users.getName());
                            }
                            break;


                        case 3:
                            System.out.println("===== 방 =====");
                            for (Channel channels : channelService.readAll()) {
                                System.out.println(channels.getRoom());
                            }
                            break;


                        case 4:
                            System.out.println("===== 메세지 =====");

                            for (Message messages : messageService.readAll()) {
                                System.out.println(messages.getTalk());
                            }
                            break;

                        default:
                            System.out.println("잘못된 번호입니다.");
                            break;
                    }
                    break;

                case 3:
                    System.out.println("===== 수정 =====");
                    System.out.println("1. 유저 수정");
                    System.out.println("2. 방 수정");
                    System.out.println("3. 메세지 수정");

                    int updateNumber = scanner.nextInt();
                    scanner.nextLine();

                    switch (updateNumber) {
                        case 1:
                            List<User> users = userService.readAll();

                            for (int i = 0; i < users.size(); i++) {

                                System.out.println((i + 1) + ". " + users.get(i).getName());
                            }

                            System.out.print("수정할 유저 선택 : ");

                            int userChoice = scanner.nextInt();
                            scanner.nextLine();

                            System.out.print("새 이름 입력 : ");

                            String newName = scanner.nextLine();

                            users.get(userChoice - 1).update(newName);

                            System.out.println("수정 완료");
                            break;

                        case 2:
                            List<Channel> channels = channelService.readAll();

                            for (int i = 0; i < channels.size(); i++) {
                                System.out.println((i + 1) + ". " + channels.get(i).getRoom());
                            }

                            System.out.print("수정할 방 선택 : ");

                            int roomChoice = scanner.nextInt();
                            scanner.nextLine();

                            System.out.print("새 방 이름 입력 : ");

                            String newRoom = scanner.nextLine();

                            channels.get(roomChoice - 1).update(newRoom);

                            System.out.println("수정 완료");
                            break;

                        case 3:
                            List<Message> messages = messageService.readAll();

                            for (int i = 0; i < messages.size(); i++) {
                                System.out.println((i + 1) + ". " + messages.get(i).getTalk());
                            }

                            System.out.print("수정할 메세지 선택 : ");

                            int messageChoice = scanner.nextInt();
                            scanner.nextLine();

                            System.out.print("새 메세지 입력 : ");

                            String newTalk = scanner.nextLine();
                            messages.get(messageChoice - 1).update(newTalk);

                            System.out.println("수정 완료");
                            break;

                        default:
                            System.out.println("잘못된 번호입니다.");
                            break;
                    }
                    break;

                case 4:
                    System.out.println("===== 삭제 =====");
                    System.out.println("1. 유저 삭제");
                    System.out.println("2. 방 삭제");
                    System.out.println("3. 메세지 삭제");

                    int deleteNumber = scanner.nextInt();
                    scanner.nextLine();

                    switch (deleteNumber) {
                        case 1:
                            List<User> users = userService.readAll();

                            for (int i = 0; i < users.size(); i++) {
                                System.out.println((i + 1) + ". " + users.get(i).getName());
                            }

                            System.out.print("삭제할 유저 선택 : ");

                            int userDelete = scanner.nextInt();
                            scanner.nextLine();

                            userService.delete(users.get(userDelete - 1).getId());

                            System.out.println("삭제 완료");
                            break;

                        case 2:
                            List<Channel> channels =
                                    channelService.readAll();

                            for (int i = 0; i < channels.size(); i++) {
                                System.out.println((i + 1) + ". " + channels.get(i).getRoom());
                            }

                            System.out.print("삭제할 방 선택 : ");
                            int channelDelete = scanner.nextInt();
                            scanner.nextLine();

                            channelService.delete(channels.get(channelDelete - 1).getId());

                            System.out.println("삭제 완료");
                            break;

                        case 3:
                            List<Message> messages = messageService.readAll();

                            for (int i = 0; i < messages.size(); i++) {
                                System.out.println(
                                        (i + 1) + ". "
                                                + messages.get(i).getTalk()
                                );
                            }

                            System.out.print("삭제할 메세지 선택 : ");
                            int messageDelete = scanner.nextInt();
                            scanner.nextLine();

                            messageService.delete(messages.get(messageDelete - 1).getId());

                            System.out.println("삭제 완료");
                            break;

                        default:
                            System.out.println("잘못된 번호입니다.");
                            break;
                    }
                    break;

                case 0:
                    System.out.println("프로그램 종료");
                    return;

                default:
                    System.out.println(
                            "번호는 0, 1, 2, 3, 4 만 눌러주십시오."
                    );
                    break;
            }
        }
    }
}