package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.Collections;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public class MessageMode {

    public static void run(MessageService messageService, Scanner scanner) {

        UUID channelId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        final UUID[] trackingId = {UUID.randomUUID()};

        System.out.print("내용을 입력하세요.: ");
        String content = scanner.nextLine();

        MessageCreateRequest createRequest = new MessageCreateRequest(
                channelId,
                senderId,
                content,
                Collections.emptyList()
        );
        MessageResponse createdResponse = messageService.create(createRequest);
        trackingId[0] = createdResponse.id();

        System.out.println(content + "메세지를 성공적으로 보냈습니다.");

        System.out.println("\n=== 방금 보낸 메세지 조회 ===");

        Optional<MessageResponse> foundMessage = messageService.findById(trackingId[0]);
        if (foundMessage.isPresent()) {
            MessageResponse newMessage = foundMessage.get();
            System.out.println("방금 보낸 메세지를 찾았습니다.");
            System.out.println("내용: " + newMessage.content());
        }

        System.out.println("\n=== 메세지 내용으로 조회 ===");
        System.out.println("조회할 메세지의 내용을 입력하세요.");
        String searchContent = scanner.nextLine();

        foundMessage = messageService.findAllByChannelId(channelId).stream()
                .filter(message -> message.content().equals(searchContent))
                .findFirst();

        if (foundMessage.isPresent()) {
            MessageResponse searchedMessage = foundMessage.get();
            System.out.println("찾은 메세지: " + searchedMessage.content()
                    + " (발송일: " + searchedMessage.createdAt() + ")");
            trackingId[0] = searchedMessage.id();
        } else {
            System.out.println("해당 내용을 가진 메세지를 찾을 수 없습니다.");
        }

        System.out.println("\n=== 전체 메세지 목록 조회 ===");
        for (MessageResponse message : messageService.findAllByChannelId(channelId)) {
            System.out.println("내용: " + message.content());
        }

        System.out.println("\n=== 메세지 수정 ===");
        System.out.println("수정할 메세지의 내용을 입력하세요: ");
        String updateContent = scanner.nextLine();

        Optional<MessageResponse> targetMessage = messageService.findAllByChannelId(channelId).stream()
                .filter(message -> message.content().equals(updateContent))
                .findFirst();

        if (targetMessage.isPresent()) {
            MessageResponse modificationTarget = targetMessage.get();
            System.out.println("새로운 메세지를 입력하세요: ");
            String newMessage = scanner.nextLine();

            messageService.update(modificationTarget.id(), new MessageUpdateRequest(newMessage));
            trackingId[0] = modificationTarget.id();

            System.out.println("내용이 '" + newMessage + "'으로 수정되었습니다.");
        } else {
            System.out.println("해당 메세지를 찾을 수 없습니다.");
        }

        System.out.println("\n=== 수정한 메세지 재확인 ===");
        Optional<MessageResponse> updateMessage = messageService.findById(trackingId[0]);

        if (updateMessage.isPresent()) {
            MessageResponse recheckedMessage = updateMessage.get();
            System.out.println("현재 저장된 내용: " + recheckedMessage.content());
            System.out.println("최종 수정 시간: " + recheckedMessage.updatedAt());
        } else {
            System.out.println("해당 내용을 가진 메세지를 찾을 수 없습니다.");
        }

        System.out.println("\n=== 메세지 삭제 ===");
        System.out.print("삭제할 메세지를 선택하세요: ");
        String deleteContent = scanner.nextLine();

        Optional<MessageResponse> deleteTarget = messageService.findAllByChannelId(channelId).stream()
                .filter(message -> message.content().equals(deleteContent))
                .findFirst();

        if (deleteTarget.isPresent()) {
            UUID deleteId = deleteTarget.get().id();
            messageService.delete(deleteId);
            System.out.println(deleteContent + " 선택한 메세지의 삭제가 완료되었습니다.");

            if (messageService.findById(deleteId).isEmpty()) {
                System.out.println("조회 결과: 해당 메세지가 존재하지 않습니다. (삭제 성공)");
            }
        } else {
            System.out.println("해당 내용을 가진 메세지가 없어 삭제에 실패했습니다.");
        }
    }
}
