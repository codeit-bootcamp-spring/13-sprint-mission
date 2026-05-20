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
                new Channel("채널", "내용 없음",
                        ChannelType.PRIVATE, user2));
        Channel channel3 = jcfChannelService.create(
                new Channel("노는 방~!", "이야기하며 놀아요!",
                        ChannelType.PUBLIC, user3));

        jcfChannelService.addMember(channel1.getId(), user2);
        jcfChannelService.addMember(channel1.getId(), user3);
        jcfChannelService.addMember(channel1.getId(), user4);
        jcfChannelService.addMember(channel1.getId(), user5);
        jcfChannelService.addMember(channel1.getId(), user6);
        jcfChannelService.addMember(channel2.getId(), user1);
        jcfChannelService.addMember(channel2.getId(), user3);
        jcfChannelService.addMember(channel2.getId(), user4);
        jcfChannelService.addMember(channel2.getId(), user5);
        jcfChannelService.addMember(channel3.getId(), user1);
        jcfChannelService.addMember(channel3.getId(), user2);
        jcfChannelService.addMember(channel3.getId(), user4);


        System.out.println("===== 채널 조회 =====");
        System.out.println(jcfChannelService.read(channel3.getId()));

        System.out.println("===== 채널 전체 조회 =====");
        for (Channel channel : jcfChannelService.readAllChannels()) {
            System.out.println(channel);
        }

        System.out.println("===== 채널 정보 수정 전 =====");
        System.out.println(jcfChannelService.read(channel2.getId()));

        System.out.println("===== 채널 정보 수정 후 =====");
        jcfChannelService.update(channel2.getId(), "독서 하는 방!",
                "함께 책 읽어요!", ChannelType.PRIVATE);
        System.out.println(jcfChannelService.read(channel2.getId()));

        System.out.println("===== 채널 정보 수정 전 (채널명) =====");
        System.out.println(jcfChannelService.read(channel1.getId()));

        System.out.println("===== 채널 정보 수정 후 (개별)");
        System.out.println(jcfChannelService.updateChannelName(
                channel1.getId(), "Java 공부하는 방!"));
        System.out.println(jcfChannelService.read(channel1.getId()));
        System.out.println(jcfChannelService.updateDescription(
                channel1.getId(), "힘내서 공부해요!"));
        System.out.println(jcfChannelService.updateChannelType(
                channel1.getId(), ChannelType.PRIVATE));

        System.out.println("===== 채널 삭제 =====");
        jcfChannelService.delete(channel3.getId());
        for (Channel channel : jcfChannelService.readAllChannels()) {
            System.out.println(channel);
        }


        System.out.println("===== 메세지 생성 =====");
        Message message1 = new Message("안녕하세요!", user1, channel1);
        jcfMessageService.create(message1);
        System.out.println(message1);
        Message message2 = new Message("여기 책 읽는 방인가요?", user2, channel1);
        jcfMessageService.create(message2);
        System.out.println(message2);
        Message message3 = new Message("아니요! Java 공부하는 방 입니다!", user1, channel1);
        jcfMessageService.create(message3);
        System.out.println(message3);
        Message message4 = new Message("다들 과제는 다 끝내셨어요?", user4, channel1);
        jcfMessageService.create(message4);
        System.out.println(message4);
        Message message5 = new Message("저는 다 끝냈어요~", user1, channel1);
        jcfMessageService.create(message5);
        System.out.println(message5);
        Message message6 = new Message("저는 아직 많이 남았어요ㅠㅠ", user3, channel1);
        jcfMessageService.create(message6);
        System.out.println(message6);
        Message message7 = new Message("저도 아직 하고있어요...ㅠㅠㅠ", user2, channel1);
        jcfMessageService.create(message7);
        System.out.println(message7);
        Message message8 = new Message("좋은 아침!", user5, channel2);
        jcfMessageService.create(message8);
        System.out.println(message8);
        Message message9 = new Message("오늘 다들 무슨 책 읽으시나요?", user2, channel2);
        jcfMessageService.create(message9);
        System.out.println(message9);
        Message message10 = new Message("엣취~~~~~!!", user5, channel2);


        System.out.println("===== 메세지 단건 조회 =====");
        System.out.println(jcfMessageService.read(message4.getId()));

        System.out.println("===== 메세지 전체 조회 =====");
        for (Message message : jcfMessageService.readAll()) {
            System.out.println(message);
        }

        // 채널별 조회 만들 예정...

        System.out.println("===== 메세지 수정 =====");
        System.out.println(jcfMessageService.read(message8.getId()));
        jcfMessageService.update(message8.getId(), "좋은 저녁입니다!");
        System.out.println(jcfMessageService.read(message8.getId()));

        System.out.println("===== 메세지 삭제 =====");
        jcfMessageService.delete(message10);
        for (Message message : jcfMessageService.readAll()) {
            System.out.println(message);
        }














    }
}
