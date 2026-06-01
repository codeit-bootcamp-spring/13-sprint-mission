package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.Scanner;
import java.util.UUID;

public class UserMode {

    public static void run(UserService userService, Scanner scanner) {

        // User

        // <이름을 입력받아 사용자 등록>
        System.out.print("사용자의 이름을 입력하세요.: "); // 사용자로부터 진짜 입력받기
        String username = scanner.nextLine();

        User inputUser = new User(username); // 입력 받은 이름으로 사용자 등록하기
        userService.create(inputUser);

        System.out.println(username + "님이 성공적으로 등록되었습니다.");

        // 방금 등록된 사용자 ID 찾아보기
        System.out.println("\n=== 방금 등록한 사용자 조회 ===");
        User foundUser = userService.findById(inputUser.getId());
        if (foundUser != null) {
            System.out.println("방금 등록한 사용자를 찾았습니다.");
            System.out.println("이름: " + foundUser.getUsername());
            System.out.println("ID: " + foundUser.getId());
        }

        // ID로 조회 (단건 조회)
        System.out.println("\n=== 사용자 ID로 조회 ===");
        System.out.println("조회할 사용자의 ID를 입력하세요.");
        String searchId = scanner.nextLine(); // 사용자로부터 ID 입력받기

        // 입력받은 글자(String)를 UUID 객체로 변환
        foundUser = userService.findById(UUID.fromString(searchId));

        // 조회 시도
        if (foundUser != null) {
            System.out.println
                    ("찾은 사용자: " + foundUser.getUsername() +
                            " (생성일: " + foundUser.getCreatedAt() + ")");
        } else {
            System.out.println("해당 ID를 가진 사용자를 찾을 수 없습니다.");
        }

        // 전체 조회 (다건 조회)
        System.out.println("\n=== 전체 사용자 목록 조회 ===");
        for (User user : userService.findAll()) {
            System.out.println("ID: " + user.getId() + " | 이름: " + user.getUsername());
        }

        // 기존에 있던 사용자 정보 수정 (이름 수정)
        System.out.println("\n=== 사용자 이름 수정 ===");
        System.out.println("수정할 사용자의 ID를 입력하세요: ");
        String updateId = scanner.nextLine();

        User targetUser = userService.findById(UUID.fromString(updateId)); // 먼저 기존 사용자를 불러오기

        if (targetUser != null) {
            System.out.println("새로운 이름을 입력하세요: ");
            String newName = scanner.nextLine();

            // 지연 후 출력
            targetUser.updateName(new User(newName));

            userService.update(targetUser);

            System.out.println("이름이 '" + newName + "'으로 수정되었습니다.");
        }else {
            System.out.println("해당 ID를 가진 사용자를 찾을 수 없습니다.");
        }

        // 수정된 정보 조회 (사용자 재검색)
        System.out.println("\n=== 수정 결과 재확인 ===");
        User updateUser = userService.findById(targetUser.getId());

        if (updateUser != null) {
            System.out.println("현재 저장된 이름: " + updateUser.getUsername());
            System.out.println("최종 수정 시간: " + updateUser.getUpdatedAt());
        }else {
            System.out.println("해당 ID를 가진 사용자를 찾을 수 없습니다.");
        }

        // 삭제
        System.out.println("\n=== 사용자 삭제 ===");
        System.out.print("삭제할 사용자의 ID를 입력하세요: ");
        String deleteId = scanner.nextLine();

        userService.delete(UUID.fromString(deleteId));
        System.out.println(deleteId + " 번 사용자의 삭제가 완료되었습니다.");

        // 조회를 통해 삭제되었는 지 확인
        if (userService.findById(UUID.fromString(deleteId)) == null) {
            System.out.println("조회 결과: 해당 사용자가 존재하지 않습니다. (삭제 성공)");
        }

    }

}
