package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import java.util.UUID;

public class JavaApplication {

    public static void main(String[] args) {

        JCFUserService jcfUserService = new JCFUserService();
        JCFChannelService jcfChannelService = new JCFChannelService();
        JCFMessageService jcfMessageService = new JCFMessageService();

        User user1 = jcfUserService.create(
                "KIMab", "kimab@discodeit.com",
                "1234", UserStatus.ONLINE);
        User user2 = jcfUserService.create(
                "LEEcd", "leecd@discodeit.com",
                "1234", UserStatus.AWAY);
        User user3 = jcfUserService.create(
                "PARKef", "parkef@discodeit.com",
                "1234",  UserStatus.GAMING);
        User user4 = jcfUserService.create(
                "JANGgh", "janggh@discodeit.com",
                "1234",  UserStatus.SLEEPING);
        User user5 = jcfUserService.create(
                "YUUij", "yuuij@discodeit.com",
                "1234",  UserStatus.STUDYING);
        User user6 = jcfUserService.create(
                "KANGtt", "kangtt@discodeit.com",
                "0987",  UserStatus.AWAY);



        System.out.println("===== 유저 단건 조회 =====");
        System.out.println(jcfUserService.read(user1.getId()));

        System.out.println("===== 유저 전체 조회 =====");
        for (User user : jcfUserService.readAll()) {
            System.out.println(user);
        }

        System.out.println("===== 유저 정보 수정 전 (전체) =====");
        System.out.println(jcfUserService.read(user2.getId()));
        jcfUserService.update(user2.getId(),
                "LEEhb",
                "5555",
                UserStatus.ONLINE);

        System.out.println("===== 유저 정보 수정 후 (전체) =====");
        System.out.println(jcfUserService.read(user2.getId()));

        System.out.println("===== 유저 정보 수정 전 (일부) =====");
        System.out.println(jcfUserService.read(user6.getId()));
        System.out.println("===== 유저 정보 수정 후 (이름) =====");
        jcfUserService.updateUsername(user6.getId(), "Codeit");
        System.out.println(jcfUserService.read(user6.getId()));
        System.out.println("===== 유저 정보 수정 후 (비밀번호) =====");
        jcfUserService.updatePassword(user6.getId(), "1234");
        System.out.println(jcfUserService.read(user6.getId()));
        System.out.println("===== 유저 정보 수정 후 (상태) =====");
        jcfUserService.updateUserStatus(user6.getId(), UserStatus.ONLINE);
        System.out.println(jcfUserService.read(user6.getId()));

        System.out.println("===== 유저 정보 삭제 =====");
        jcfUserService.delete(user6.getId());
        for (User user : jcfUserService.readAll()) {
            System.out.println(user);
        }


        Channel channel1 = jcfChannelService.create(
                new Channel("채널", "Java 공부해요!",
                        ChannelType.PUBLIC, user1));
        Channel channel2 = jcfChannelService.create(
                new Channel("채널", "함께 책 읽어요!",
                        ChannelType.PRIVATE, user2));
        Channel channel3 = jcfChannelService.create(
                new Channel("노는 방~!", "이야기하며 놀아요!",
                        ChannelType.PUBLIC, user3));

        System.out.println("===== 채널 조회 =====");
        System.out.println(jcfChannelService.read(channel1.getId()));

        System.out.println("===== 채널 전체 조회 =====");
        for (Channel channel : jcfChannelService.readAllChannels()) {
            System.out.println(channel);
        }

        System.out.println("===== 채널 정보 수정 전 =====");
        System.out.println(jcfChannelService.read(channel1.getId()));







    }
}
