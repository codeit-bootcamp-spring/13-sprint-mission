package com.sprint.mission.discodeit.entity;


import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.file.FileChannelService;
import com.sprint.mission.discodeit.service.file.FileMessageService;
import com.sprint.mission.discodeit.service.file.FileUserService;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static com.sprint.mission.discodeit.entity.ChannelType.PRIVATE;
import static com.sprint.mission.discodeit.entity.ChannelType.PUBLIC;

public class JavaApplication {
    public static void main(String[] args) throws IOException {

        // User: UUID id, Long createdAt, updatedAt(수정 시에만 갱신), String username, email
        // Channel: UUID id, Long createdAt, updatedAt, ChannelType type, String name
        // Message: UUID id, Long createdAt, updatedAt, String content, UUID channelId



        UserService userService=new FileUserService();
        //UserService userService=new JCFUserService(); // 객체 생성
        // 등록 register
        User user1=userService.createOne("오시온","sion0511@icloud.com", System.currentTimeMillis());
        User user2=userService.createOne("마에다","maed0628@icloud.com", System.currentTimeMillis());
        User user3=userService.createOne("구정모", "jungmo0205@icloud.com", System.currentTimeMillis());



        //MessageService messageService=new JCFMessageService();
        MessageService messageService=new FileMessageService();
        Message directMessage=messageService.createOne(UUID.randomUUID(), "클래스 매니저님과 나눈 다이렉트 메시지의 첫 부분이에요.", System.currentTimeMillis());
        Message directMessage2=messageService.createOne(UUID.randomUUID(), "회원님의 활동을 응원합니다.", System.currentTimeMillis());



        //ChannelService channelService=new JCFChannelService();
        ChannelService channelService=new FileChannelService();
        Channel notice=channelService.createOne(PUBLIC, "공지", System.currentTimeMillis());
        Channel ask=channelService.createOne(PRIVATE, "학습", System.currentTimeMillis());
        Channel ask2=channelService.createOne(PUBLIC, "학습-공지", System.currentTimeMillis());

        // 조회 (단건, 다건) check

        System.out.println("=== 사용자 현황 ===");
        Optional<User> read=userService.readOne(user1.getId());
        System.out.println("온라인--단건\n"+ read.get().getUsername()+" ("+ read.get().getEmail()+")");
        System.out.println("온라인--다건");
        userService.readAll().forEach(user -> System.out.println(user.getUsername()+" ("+user.getEmail()+")"));

        System.out.println();
        System.out.println("=== 다이렉트 메시지 ===");
        Optional<Message> read2 =messageService.readOne(directMessage.getId());
        System.out.println("메시지--단건\n("+ read2.get().getChannelId()+") "+ read2.get().getContent());
        System.out.println("메시지--다건");
        messageService.readAll().forEach(message -> System.out.println("("+message.getChannelId()+") "+message.getContent()));


        System.out.println();
        System.out.println("=== 채널 ===");
        Optional<Channel> read3 =channelService.readOne(notice.getId());
        System.out.println("채널--단건\n"+ read3.get().getName()+" ("+ read3.get().getType()+")");
        System.out.println("채널--다건");
        channelService.readAll().forEach(channel-> System.out.println(channel.getName()+" ("+channel.getType()+")"));




        // 수정 edit
        // 수정된 데이터 조회
        System.out.println();

        System.out.println("=== 프로필 수정 ====");
        userService.editOne(user1.getId(),"한유진","yujin0320@icloud.com", System.currentTimeMillis());
        userService.editOne(user3.getId(),"문상민","sangminicloud.com", System.currentTimeMillis());
        System.out.println();
        System.out.println("=== 수정된 사용자 조회 ====");
        System.out.println("온라인 -- ");
        userService.readAll().forEach(user -> System.out.println(user.getUsername()+" ("+user.getEmail()+")"));

        System.out.println();

        System.out.println("=== 메시지 수정 ====");
        messageService.editOne(directMessage2.getId(), UUID.randomUUID(), "오늘 멘토링 일정은 오후 7시입니다.", System.currentTimeMillis());
        messageService.editOne(directMessage.getId(), UUID.randomUUID(), "", System.currentTimeMillis());
        System.out.println();
        System.out.println("=== 수정된 메시지 조회 ====");
        System.out.println("다이렉트 메시지 +");
        messageService.readAll().forEach(message -> System.out.println("("+message.getChannelId()+") "+message.getContent()));
        System.out.println();


        System.out.println("=== 채널 수정 ====");
        channelService.editOne(ask2.getId(),  PRIVATE, "zep-이슈제보", System.currentTimeMillis());
        channelService.editOne(ask.getId(), PUBLIC, "", System.currentTimeMillis());
        System.out.println();
        System.out.println("=== 수정된 채널 조회 ====");
        System.out.println("현재 서버 >");
        channelService.readAll().forEach(channel-> System.out.println(channel.getName()+" ("+channel.getType()+")"));

        System.out.println();

        // 삭제 delete
        // 조회 통해 삭제되었는지 확인
        System.out.println("=== 사용자 삭제 ====");
        userService.deleteOne(user2.getId());
        Optional<User> deleted=userService.readOne(user2.getId());
        if(deleted.isEmpty()) System.out.println("계정 삭제!");

        System.out.println();
        System.out.println("=== 메시지 삭제 ====");
        messageService.deleteOne(directMessage.getId());
        Optional<Message> deleted2=messageService.readOne(directMessage.getId());
        if(deleted2.isEmpty()) System.out.println("메시지 삭제!");

        System.out.println();
        System.out.println("=== 채널 삭제 ====");
        channelService.deleteOne(notice.getId());
        Optional<Channel> deleted3=channelService.readOne(notice.getId());

        if(deleted3.isEmpty()) System.out.println("채널 삭제!");



        System.out.println();



    }
}
