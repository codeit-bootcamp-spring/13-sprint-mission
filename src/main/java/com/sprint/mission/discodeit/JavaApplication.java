package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.file.FileChannelService;
import com.sprint.mission.discodeit.service.file.FileMessageService;
import com.sprint.mission.discodeit.service.file.FileUserService;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import java.util.Scanner;
import java.util.UUID;

import static com.sprint.mission.discodeit.SampleData.names;

public class JavaApplication {

    public  static void main(String[] args) {


        // <기존 데이터 등록
        UserService userService = new FileUserService(); // 2차 미션 때는 File로 갈아 끼우기
        ChannelService channelService = new FileChannelService();
        MessageService messageService = new FileMessageService();
        Scanner scanner = new Scanner(System.in); // 키보드 입력기

        SampleData.loadSampleNames(); // 기존 데이터 먼저 준비
        SampleData.loadChannelTitles(); // 기존 데이터 먼저 준비
        SampleData.loadMessages();

        for (String name : SampleData.names) { // 기존 데이터 먼저 등록
            userService.create(new User(name));
        }
        // <기존 채널 등록>
        for (String title : SampleData.titles) { // 기존 데이터 먼저 등록
            channelService.create(new Channel(title));
        }
        for (String message : SampleData.messages) {
            messageService.create(new Message(message));
        }

        boolean running = true;

        while (running) {
            System.out.println("\n==== 메인 메뉴 ====");
            System.out.println("1. User");
            System.out.println("2. Channel");
            System.out.println("3. Message");
            System.out.println("0. 종료");
            System.out.print("선택: ");

            int choice = Integer.parseInt(scanner.nextLine()); // 숫자 입력 받기

            switch (choice) {
                case 1: UserMode.run(userService, scanner);
                        break;
                case 2: ChannelMode.run(channelService, scanner);
                        break;
                case 3: MessageMode.run(messageService, scanner);
                        break;
                case 0: System.out.println("프로그램을 종료합니다.");
                        running = false;
                        break;
                default: System.out.println("잘못된 입력입니다.");
            }
        }

    }

}
