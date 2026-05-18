package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.Scanner;
import java.util.UUID;

public class ChannelMode {

    private final static ChannelService channelService;
    private final static Scanner scanner;

    public ChannelMode(ChannelService channelService, Scanner scanner) {
        this.channelService = channelService;
        this.scanner = scanner;
    }
    private static void run() {

        // Channel

        // <글자를 입력받아 채널 등록>
        System.out.print("제목을 입력하세요.: "); // 사용자로부터 진짜 입력받기
        String channelTitles = scanner.nextLine();

        Channel inputChannel = new Channel(channelTitles); // 입력 받은 제목으로 채널 등록하기
        channelService.create(inputChannel);

        System.out.println(channelTitles + "채널이 성공적으로 생성되었습니다.");

        // 방금 등록된 채널 ID 찾아보기
        System.out.println("\n=== 방금 생성한 채널 조회 ===");
        Channel foundChannel = channelService.findById(inputChannel.getId());
        if (foundChannel != null) {
            System.out.println("방금 생성한 채널을 찾았습니다.");
            System.out.println("제목: " + foundChannel.getChannelTitles());
            System.out.println("ID: " + foundChannel.getId());
        }

        // ID로 조회 (단건 조회)
        System.out.println("\n=== 채널 ID로 조회 ===");
        System.out.println("조회할 채널의 ID를 입력하세요.");
        String searchId = scanner.nextLine(); // 사용자로부터 ID 입력받기

        // 입력받은 글자(String)를 UUID 객체로 변환
        foundChannel = channelService.findById(UUID.fromString(searchId));

        // 조회 시도
        if (foundChannel != null) {
            System.out.println
                    ("찾은 채널: " + foundChannel.getChannelTitles() +
                            " (생성일: " + foundChannel.getCreatedAt() + ")");
        } else {
            System.out.println("해당 ID를 가진 채널을 찾을 수 없습니다.");
        }

        // 전체 조회 (다건 조회)
        System.out.println("\n=== 전체 채널 목록 조회 ===");
        for (Channel channel : channelService.findAll()) {
            System.out.println("ID: " + channel.getId() + " | 제목: " + channel.getChannelTitles());
        }

        // 기존에 있던 채널 정보 수정 (제목 수정)
        System.out.println("\n=== 채널 제목 수정 ===");
        System.out.println("수정할 채널의 ID를 입력하세요: ");
        String updateId = scanner.nextLine();

        Channel targetChannel = channelService.findById(UUID.fromString(updateId)); // 먼저 기존 사용자를 불러오기

        if (targetChannel != null) {
            System.out.println("새로운 제목을 입력하세요: ");
            String newChannel = scanner.nextLine();

            // 지연 후 출력
            targetChannel.updateTitles(new Channel(newChannel));

            channelService.update(targetChannel);

            System.out.println("제목이 '" + newChannel + "'으로 수정되었습니다.");
        }else {
            System.out.println("해당 ID를 가진 채널을 찾을 수 없습니다.");
        }

        // 수정된 정보 조회 (채널 재검색)
        System.out.println("\n=== 수정 결과 재확인 ===");
        Channel updateChannel = channelService.findById(targetChannel.getId());

        if (updateChannel != null) {
            System.out.println("현재 저장된 제목: " + updateChannel.getChannelTitles());
            System.out.println("최종 수정 시간: " + updateChannel.getUpdatedAt());
        }else {
            System.out.println("해당 ID를 가진 채널을 찾을 수 없습니다.");
        }

        // 삭제
        System.out.println("\n=== 채널 삭제 ===");
        System.out.print("삭제할 채널의 ID를 입력하세요: ");
        String deleteId = scanner.nextLine();

        channelService.delete(UUID.fromString(deleteId));
        System.out.println(deleteId + " 번 채널의 삭제가 완료되었습니다.");

        // 조회를 통해 삭제되었는 지 확인
        if (channelService.findById(UUID.fromString(deleteId)) == null) {
            System.out.println("조회 결과: 해당 채널이 존재하지 않습니다. (삭제 성공)");
        }

    }
}
