package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


import java.util.List;

@SpringBootApplication
public class DiscodeitApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

        // 컨텍스트에서 Bean 꺼내오기
        UserService userService = context.getBean(UserService.class);
        ChannelService channelService = context.getBean(ChannelService.class);
        MessageService messageService = context.getBean(MessageService.class);



        // 1. 등록 테스트 (3명 등록)
        User user1 = new User();
        User user2 = new User();
        User user3 = new User();

        userService.save(user1);
        userService.save(user2);
        userService.save(user3);
        System.out.println("유저 3명 등록 완료.");

        // 2. 전체 조회 테스트 (추가된 부분)
        System.out.println("\n-- 전체 유저 목록 조회 --");
        List<User> allUsers = userService.findAll();
        for (User u : allUsers) {
            System.out.println("ID: " + u.getId() + ", 생성시간: " + u.getCreatedAt());
        }

        // 3. 단일 조회
        userService.findById(user1.getId()).ifPresent(foundUser -> {
            System.out.println("\n단일 조회 성공: " + foundUser.getId());
        });

        // 4. 수정
        user1.update();
        userService.update(user1);
        System.out.println("수정 완료: " + user1.getUpdatedAt());

        // 5. 삭제
        userService.delete(user2.getId());
        System.out.println("\nID " + user2.getId() + " 삭제 후 전체 목록 조회:");

        // 6. 삭제 후 전체 조회
        List<User> remainingUsers = userService.findAll();
        remainingUsers.forEach(u -> System.out.println("남은 ID: " + u.getId()));
    }
}