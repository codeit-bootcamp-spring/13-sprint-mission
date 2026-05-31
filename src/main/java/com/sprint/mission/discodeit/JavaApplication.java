package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.jcf.*;
import com.sprint.mission.discodeit.service.*;

import java.util.*;

public class JavaApplication {

    public static void main(String[] args) {

        UserService userService = new JCFUserService();
        ChannelService channelService = new JCFChannelService();
        MessageService messageService = new JCFMessageService();

        System.out.println("================== [User 도메인 테스트] ==================");

        // create 등록
        // userService 안에서 생성된 final유일한 data에 계속 정보가 추가됨.
        List<User> users = new ArrayList<>();
        users.add(new User("홍길동", "qwer1234", "aaaa@naveer.com", "010-1111-2323"));
        users.add(new User("김철수", "q1w2e3r4", "meme@codeit.com", "010-1233-1244"));
        users.add(new User("오바마", "zx111", "vcvcvcvvc@gmail.com", "010-6323-5555"));
        users.add(new User("트럼프", "fdfdfdfdfd", "sdl123@naveer.com", "010-9999-2913"));
        System.out.println("1. 유저 등록");
        for (User user : users) {
            userService.create(user);
            System.out.println("유저 등록 완료 (User ID: :" + user.getId() + " | 등록시간 : "  + user.getCreatedAt() + " ) ");
            System.out.println(" - userName : " + user.getUserName() + " | password: " + user.getPassword() + " | email: " + user.getEmail() + " | phoneNumber: " + user.getPhoneNumber());
            System.out.println();
        }

        // 조회
        UUID targetUserId1 = users.get(2).getId();
        System.out.println("2. 전체 조회: 현재 유저 수 " + userService.findAll().size() + "명");
        for (User user : userService.findAll()) {
            System.out.println(" - userName : " + user.getUserName() + " | password: " + user.getPassword() + " | email: " + user.getEmail() + " | phoneNumber: " + user.getPhoneNumber());
            System.out.println();
        }
        System.out.println("단건 조회: 찾은 유저 이름 -> " + userService.findById(targetUserId1).getUserName());
        System.out.println();

        // 수정
        String oldUserName = userService.findById(targetUserId1).getUserName();
        String newUserName = "박똥개";
        userService.findById(targetUserId1).updateUserName(newUserName);
        userService.update(userService.findById(targetUserId1));
        System.out.println("3. 유저 이름변경 완료");
        User updatedUser = userService.findById(targetUserId1);
        System.out.println("변경전 이름 : " + oldUserName + " / 변경후 이름 : " + updatedUser.getUserName() + " (업데이트 시간 : " + updatedUser.getUpdatedAt() + ")");
        System.out.println();

        // 삭제
        userService.delete(targetUserId1);
        System.out.println("4. 삭제 완료");
        System.out.println("전체 조회: 현재 유저 수 " + userService.findAll().size() + "명");
        for (User user : userService.findAll()) {
            System.out.println(" - userName : " + user.getUserName() + " | password: " + user.getPassword() + " | email: " + user.getEmail() + " | phoneNumber: " + user.getPhoneNumber());
            System.out.println();
        }






        System.out.println("================== [Channel 도메인 테스트] ==================");

        // 등록
        List<Channel> channels = new ArrayList<>();
        channels.add(new Channel("공지", ChannelType.CHATTING,"전체 공지"));
        channels.add(new Channel("코드공유", ChannelType.CHATTING,"코드를 공유하는공간"));
        channels.add(new Channel("문의", ChannelType.CHATTING,"건의사항 문의 등"));
        channels.add(new Channel("전체보이스", ChannelType.VOICE, "잡담 공간"));
        channels.add(new Channel("강의", ChannelType.VOICE,  "강의가 진행되는 곳"));
        System.out.println("1. 채널 등록");
        for (Channel channel : channels) {
            channelService.create(channel);
            System.out.println("채널 등록 완료 (Channel ID: :" + channel.getId() + " | 등록시간 : " + channel.getChannelName() + " )" );
            System.out.println(" - channelType: " + channel.getChannelType() + " | channelName: " + channel.getChannelName() + " | channelDescription: " + channel.getChannelDescription());
            System.out.println();
        }
        System.out.println();

        // 조회
        UUID targetUserId2 = channels.get(1).getId();
        System.out.println("2. 전체 조회: 현재 채널 수 " + channelService.findAll().size() + "개");
        for (Channel channel : channelService.findAll()) {
            System.out.println(" - channelName : " + channel.getChannelName() + " | channelType: " + channel.getChannelType() + " | channelDescription " + channel.getChannelDescription());
            System.out.println();
        }
        System.out.println("단건 조회: 찾은 채널 이름 -> " + channelService.findById(targetUserId2).getChannelName());
        System.out.println();

        // 수정
        String oldChannelName = channelService.findById(targetUserId2).getChannelName();
        String newChannelName = "Q&A";
        channelService.findById(targetUserId2).updateChannelName(newChannelName);
        channelService.update(channelService.findById(targetUserId2));
        System.out.println("3. 채널 이름변경 완료");
        Channel updatedChannel = channelService.findById(targetUserId2);
        System.out.println("변경전 채널 이름 : " + oldChannelName + " / 변경후 채널 이름 : " + updatedChannel.getChannelName() + " (업데이트 시간 : " + updatedChannel.getUpdatedAt() + ")");
        System.out.println();

        // 삭제
        channelService.delete(targetUserId2);
        System.out.println("4. 채널 삭제 완료");
        System.out.println("전체 조회: 현재 채널 수 " + channelService.findAll().size() + "개");
        for (Channel channel : channelService.findAll()) {
            System.out.println(" - channelName : " + channel.getChannelName() + " | channelType: " + channel.getChannelType() + " | channelDescription " + channel.getChannelDescription());
            System.out.println();
        }




        System.out.println("================== [Message 도메인 테스트] ==================");

        // 등록
        // 임의 채널,유저 가져오기
        UUID user1 = users.get(0).getId();
        UUID user2 = users.get(1).getId();
        UUID channel1 = channels.get(0).getId();
        UUID channel2 = channels.get(1).getId();

        List<Message> messages = new ArrayList<>();
        messages.add(new Message("5/1일 행정공지입니다. 참고해주세요", channel1, user1));
        messages.add(new Message("6/1일 행정공지입니다. 참고해주세요", channel1, user2));
        messages.add(new Message("5/20일 코드공유입니다. 복사해서 사용해주세요", channel2, user1));
        messages.add(new Message("6/10일 코드공유입니다. 복사해서 사용해주세요", channel2, user2));
        System.out.println("1. 메세지 등록");
        for (Message message : messages) {
            messageService.create(message);
            System.out.println("메세지 등록 완료 (Message ID: :" + message.getId() + " | 등록시간 : " + message.getCreatedAt() + " )");
            System.out.println(" - content: " + message.getContent() + " | channelId: " + message.getChannelId() + " | authorId: " + message.getAuthorId());
            System.out.println();
        }
        System.out.println();

        // 조회
        UUID targetUserId3 = messages.get(3).getId();
        System.out.println("2. 전체 조회: 현재 메세지 수 " + messageService.findAll().size() + "개");
        for (Message message : messageService.findAll()) {
            System.out.println(" - content: " + message.getContent() + " | channelId: " + message.getChannelId() + " | authorId: " + message.getAuthorId());
            System.out.println();
        }
        System.out.println("단건 조회: 찾은 메세지 내용 -> " + messageService.findById(targetUserId3).getContent());
        System.out.println();

        // 수정
        String oldContent = messageService.findById(targetUserId3).getContent();
        String newContent = "오늘 강의 코드 공유입니다. 자유롭게 사용해주세요";
        messageService.findById(targetUserId3).updateContent(newContent);
        messageService.update(messageService.findById(targetUserId3));
        System.out.println("3. 채널 이름변경 완료");
        Message updateContent = messageService.findById(targetUserId3);
        System.out.println("변경전 메세지 내용 : " + oldContent + " / 변경후 메세지 내용 : " + updateContent.getContent() + " (업데이트 시간 : " + updateContent.getUpdatedAt() + ")");
        System.out.println();

        // 삭제
        messageService.delete(targetUserId3);
        System.out.println("4. 메세지 삭제 완료");
        System.out.println("전체 조회: 현재 메세지 수 " + messageService.findAll().size() + "개");
        for (Message message : messageService.findAll()) {
            System.out.println(" - content: " + message.getContent() + " | channelId: " + message.getChannelId() + " | authorId: " + message.getAuthorId());
            System.out.println();
        }
    }

}
