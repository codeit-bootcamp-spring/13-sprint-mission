package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.service.file.*;





public class JavaApplication {

    public static void main(String[] args) {

//        JCFUserService jcfUserService = new JCFUserService();
//        JCFMessageService jcfMessageService = new JCFMessageService();
//        JCFChannelService jcfChannelService = new JCFChannelService();

        FileUserService fileUserService= new FileUserService();
        FileMessageService fileMessageService = new FileMessageService();
        FileChannelService fileChannelService = new FileChannelService();


        User user1 = fileUserService.create(
                "KIMab", "kimab@discodeit.com",
                "1234", UserStatus.ONLINE);
        User user2 = fileUserService.create(
                "LEEcd", "leecd@discodeit.com",
                "1234", UserStatus.AWAY);
        User user3 = fileUserService.create(
                "PARKef", "parkef@discodeit.com",
                "1234",  UserStatus.GAMING);
        User user4 = fileUserService.create(
                "JANGgh", "janggh@discodeit.com",
                "1234",  UserStatus.SLEEPING);
        User user5 = fileUserService.create(
                "YUUij", "yuuij@discodeit.com",
                "1234",  UserStatus.STUDYING);
        User user6 = fileUserService.create(
                "KANGtt", "kangtt@discodeit.com",
                "0987",  UserStatus.AWAY);



        System.out.println("===== 유저 단건 조회 =====");
        System.out.println(fileUserService.read(user1.getId()));

        System.out.println("===== 유저 전체 조회 =====");
        for (User user : fileUserService.readAll()) {
            System.out.println(user);
        }

        System.out.println("===== 유저 정보 수정 전 (전체) =====");
        System.out.println(fileUserService.read(user2.getId()));
        fileUserService.update(user2.getId(),
                "LEEhb",
                "5555",
                UserStatus.ONLINE);

        System.out.println("===== 유저 정보 수정 후 (전체) =====");
        System.out.println(fileUserService.read(user2.getId()));

        System.out.println("===== 유저 정보 수정 전 (일부) =====");
        System.out.println(fileUserService.read(user6.getId()));
        System.out.println("===== 유저 정보 수정 후 (이름) =====");
        fileUserService.updateUsername(user6.getId(), "Codeit");
        System.out.println(fileUserService.read(user6.getId()));
        System.out.println("===== 유저 정보 수정 후 (비밀번호) =====");
        fileUserService.updatePassword(user6.getId(), "1234");
        System.out.println(fileUserService.read(user6.getId()));
        System.out.println("===== 유저 정보 수정 후 (상태) =====");
        fileUserService.updateUserStatus(user6.getId(), UserStatus.ONLINE);
        System.out.println(fileUserService.read(user6.getId()));

        System.out.println("===== 유저 정보 삭제 =====");
        fileUserService.delete(user6.getId());
        for (User user : fileUserService.readAll()) {
            System.out.println(user);
        }


        Channel channel1 = fileChannelService.create(
                new Channel("채널", "Java 공부해요!",
                        ChannelType.PUBLIC, user1));
        Channel channel2 = fileChannelService.create(
                new Channel("채널", "내용 없음",
                        ChannelType.PRIVATE, user2));
        Channel channel3 = fileChannelService.create(
                new Channel("노는 방~!", "이야기하며 놀아요!",
                        ChannelType.PUBLIC, user3));

        fileChannelService.addMember(channel1.getId(), user2);
        fileChannelService.addMember(channel1.getId(), user3);
        fileChannelService.addMember(channel1.getId(), user4);
        fileChannelService.addMember(channel1.getId(), user5);
        fileChannelService.addMember(channel2.getId(), user1);
        fileChannelService.addMember(channel2.getId(), user3);
        fileChannelService.addMember(channel2.getId(), user4);
        fileChannelService.addMember(channel2.getId(), user5);
        fileChannelService.addMember(channel3.getId(), user1);
        fileChannelService.addMember(channel3.getId(), user2);
        fileChannelService.addMember(channel3.getId(), user4);


        System.out.println("===== 채널 조회 =====");
        System.out.println(fileChannelService.read(channel3.getId()));

        System.out.println("===== 채널 전체 조회 =====");
        for (Channel channel : fileChannelService.readAllChannels()) {
            System.out.println(channel);
        }

        System.out.println("===== 채널 정보 수정 전 =====");
        System.out.println(fileChannelService.read(channel2.getId()));

        System.out.println("===== 채널 정보 수정 후 =====");
        fileChannelService.update(channel2.getId(), "독서 하는 방!",
                "함께 책 읽어요!", ChannelType.PRIVATE);
        System.out.println(fileChannelService.read(channel2.getId()));

        System.out.println("===== 채널 정보 수정 전 (채널명) =====");
        System.out.println(fileChannelService.read(channel1.getId()));

        System.out.println("===== 채널 정보 수정 후 (개별)");
        System.out.println(fileChannelService.updateChannelName(
                channel1.getId(), "Java 공부하는 방!"));
        System.out.println(fileChannelService.read(channel1.getId()));
        System.out.println(fileChannelService.updateDescription(
                channel1.getId(), "힘내서 공부해요!"));
        System.out.println(fileChannelService.updateChannelType(
                channel1.getId(), ChannelType.PRIVATE));

        System.out.println("===== 채널 삭제 =====");
        fileChannelService.delete(channel3.getId());
        for (Channel channel : fileChannelService.readAllChannels()) {
            System.out.println(channel);
        }


        System.out.println("===== 메세지 생성 =====");
        Message message1 = new Message("안녕하세요!", user1, channel1);
        fileMessageService.create(message1);
        System.out.println(message1);
        Message message2 = new Message("여기 책 읽는 방인가요?", user2, channel1);
        fileMessageService.create(message2);
        System.out.println(message2);
        Message message3 = new Message("아니요! Java 공부하는 방 입니다!", user1, channel1);
        fileMessageService.create(message3);
        System.out.println(message3);
        Message message4 = new Message("다들 과제는 다 끝내셨어요?", user4, channel1);
        fileMessageService.create(message4);
        System.out.println(message4);
        Message message5 = new Message("저는 다 끝냈어요~", user1, channel1);
        fileMessageService.create(message5);
        System.out.println(message5);
        Message message6 = new Message("저는 아직 많이 남았어요ㅠㅠ", user3, channel1);
        fileMessageService.create(message6);
        System.out.println(message6);
        Message message7 = new Message("저도 아직 하고있어요...ㅠㅠㅠ", user2, channel1);
        fileMessageService.create(message7);
        System.out.println(message7);
        Message message8 = new Message("좋은 아침!", user5, channel2);
        fileMessageService.create(message8);
        System.out.println(message8);
        Message message9 = new Message("오늘 다들 무슨 책 읽으시나요?", user2, channel2);
        fileMessageService.create(message9);
        System.out.println(message9);
        Message message10 = new Message("엣취~~~~~!!", user5, channel2);
        fileMessageService.create(message10);
        System.out.println(message10);


        System.out.println("===== 메세지 단건 조회 =====");
        System.out.println(fileMessageService.read(message4.getId()));

        System.out.println("===== 메세지 전체 조회 =====");
        for (Message message : fileMessageService.readAll()) {
            System.out.println(message);
        }

        // 채널별 조회 만들 예정...

        System.out.println("===== 메세지 수정 =====");
        System.out.println(fileMessageService.read(message8.getId()));
        fileMessageService.update(message8.getId(), "좋은 저녁입니다!");
        System.out.println(fileMessageService.read(message8.getId()));

        System.out.println("===== 메세지 삭제 =====");
        fileMessageService.delete(message10);
        for (Message message : fileMessageService.readAll()) {
            System.out.println(message);
        }









    }
}
