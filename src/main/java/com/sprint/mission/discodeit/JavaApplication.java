package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.request.ChannelPublicRequest;
import com.sprint.mission.discodeit.dto.request.UserRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.file.FileChannelService;
import com.sprint.mission.discodeit.service.file.FileMessageService;
import com.sprint.mission.discodeit.service.file.FileUserService;

import java.util.Scanner;

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

        for (String name : SampleData.names) {
            UserRequest sampleRequest = new UserRequest(
                    name,
                    name + "@codeit.com",
                    "password123!",
                    null
            );
            userService.create(sampleRequest);
        }
        // <기존 채널 등록>
        for (String title : SampleData.titles) { // 기존 데이터 먼저 등록
            ChannelPublicRequest sampleChannel = new ChannelPublicRequest(title, "샘플 채널 설명");
//            channelService.createPrivateChannel(new Channel(title));
            channelService.createPublicChannel(sampleChannel);
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
