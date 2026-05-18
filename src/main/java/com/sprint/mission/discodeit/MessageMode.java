package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.Scanner;
import java.util.UUID;

public class MessageMode {

    private static final MessageService messageService;
    private static final Scanner scanner;

    public MessageMode(MessageService messageService, Scanner scanner) {
        this.messageService = messageService;
        this.scanner = scanner;
    }

    private static void run() {

        // Message
        // <글자를 입력받아 메세지 추가>
        System.out.print("내용을 입력하세요.: "); // 사용자로부터 진짜 입력받기
        String content = scanner.nextLine();

        Message inputMessage = new Message(content); // 입력 받은 글자로 메세지 등록하기
        messageService.create(inputMessage);

        System.out.println(content + "메세지를 성공적으로 보냈습니다.");

        // 방금 등록된 메세지 ID 찾아보기
        System.out.println("\n=== 방금 보낸 메세지 조회 ===");
        Message foundMessage = messageService.findById(inputMessage.getId());
        if (foundMessage != null) {
            System.out.println("방금 보낸 메세지를 찾았습니다.");
            System.out.println("제목: " + foundMessage.getMessageTitles());
            System.out.println("내용: " + foundMessage.getId());
        }

        // ID로 조회 (단건 조회)
        System.out.println("\n=== 메세지 내용으로 조회 ===");
        System.out.println("조회할 메세지의 내용을 입력하세요.");
        String searchId = scanner.nextLine(); // 사용자로부터 ID 입력받기

        // 입력받은 글자(String)를 UUID 객체로 변환
        foundMessage = messageService.findById(UUID.fromString(searchId));

        // 조회 시도
        if (foundMessage != null) {
            System.out.println
                    ("찾은 메세지: " + foundMessage.getMessageTitles() +
                            " (발송일: " + foundMessage.getCreatedAt() + ")");
        } else {
            System.out.println("해당 내용을 가진 메세지를 찾을 수 없습니다.");
        }

        // 전체 조회 (다건 조회)
        System.out.println("\n=== 전체 메세지 목록 조회 ===");
        for (Message message : messageService.findAll()) {
            System.out.println("내용: " + message.getId() + " | 제목: " + message.getMessageTitles());
        }

        // 기존에 있던 메세지 수정 (내용 수정)
        System.out.println("\n=== 메세지 수정 ===");
        System.out.println("수정할 메세지의 내용을 입력하세요: ");
        String updateId = scanner.nextLine();

        Message targetMessage = messageService.findById(UUID.fromString(updateId)); // 먼저 기존 사용자를 불러오기

        if (targetMessage != null) {
            System.out.println("새로운 메세지를 입력하세요: ");
            String newMessage = scanner.nextLine();

            // 지연 후 출력
            targetMessage.updateTitles(new Message(newMessage));

            messageService.update(targetMessage);

            System.out.println("내용이 '" + newMessage + "'으로 수정되었습니다.");
        }else {
            System.out.println("해당 메세지를 찾을 수 없습니다.");
        }

        // 수정된 정보 조회 (메세지 재검색)
        System.out.println("\n=== 수정한 메세지 재확인 ===");
        Message updateMessage = messageService.findById(targetMessage.getId());

        if (updateMessage != null) {
            System.out.println("현재 저장된 내용: " + updateMessage.getMessageTitles());
            System.out.println("최종 수정 시간: " + updateMessage.getUpdatedAt());
        }else {
            System.out.println("해당 내용을 가진 메세지를 찾을 수 없습니다.");
        }

        // 삭제
        System.out.println("\n=== 메세지 삭제 ===");
        System.out.print("삭제할 메세지를 선택하세요: ");
        String deleteId = scanner.nextLine();

        messageService.delete(UUID.fromString(deleteId));
        System.out.println(deleteId + " 선택한 메세지의 삭제가 완료되었습니다.");

        // 조회를 통해 삭제되었는 지 확인
        if (messageService.findById(UUID.fromString(deleteId)) == null) {
            System.out.println("조회 결과: 해당 메세지가 존재하지 않습니다. (삭제 성공)");
        }

    }
}
