package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.file.FileChannelService;
import com.sprint.mission.discodeit.service.file.FileMessageService;
import com.sprint.mission.discodeit.service.file.FileUserService;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class JavaApplication {
    public static void main(String[] args) {
        UserRepository userRepository = new FileUserRepository("data/users.ser");
        ChannelRepository channelRepository = new FileChannelRepository("data/channels.ser");
        MessageRepository messageRepository = new FileMessageRepository("data/messages.ser");

        UserService userService = new FileUserService(userRepository);
        ChannelService channelService = new FileChannelService(channelRepository);
        MessageService messageService = new FileMessageService(messageRepository, userService, channelService);

        System.out.println("========= [ 1. 등   록 ] =========");
        System.out.println(" 1) 유저 등록 ");
        User user1 = userService.create("이예은", "aaa123", "aaa@aaa.com");
        User user2 = userService.create("강다연", "bbb456", "bbb@bbb.com");
        User user3 = userService.create("장준서", "ccc789", "ccc@ccc.com");
        User user4 = userService.create("함지원", "ddd012", "ddd@ddd.com");
        System.out.println("--------------------------------");

        System.out.println(" 2) 채널 등록");
        Channel channel1 = channelService.create(ChannelType.PUBLIC, "자유게시판", "자유롭게 사용하시면 됩니다.");
        Channel channel2 = channelService.create(ChannelType.PUBLIC, "공지게시판", "중요한 공지사항이 올라오는 곳입니다.");
        Channel channel3 = channelService.create(ChannelType.PRIVATE, "1팀게시판", "1팀들만 사용가능한 비공개 게시판입니다.");
        System.out.println("--------------------------------");

        System.out.println(" 3) 메시지 등록");
        Message message1 = messageService.create(user1.getId(), channel1.getId(), "안녕 얘들아!");
        Message message2 = messageService.create(user2.getId(), channel2.getId(), "공지사항: ~5/31 스프린트 미션2 제출");
        Message message3 = messageService.create(user3.getId(), channel3.getId(), "1팀 오늘 회의~");
        System.out.println();


        System.out.println("========= [ 2. 조   회 ] =========");
        System.out.println(" 1) 단건 조회 ");
        User readUser = userService.read(user1.getId());
        System.out.println(" 유저 단건 조회: "+readUser.getUserName()+"님의 계정이 존재합니다.");
        Channel readChannel = channelService.read(channel1.getId());
        System.out.println(" 채널 단건 조회: "+readChannel.getChannelName()+"이 존재합니다.");
        Message readMessage = messageService.read(message1.getId());
        System.out.println(" 메시지 단건 조회: "+readMessage.getContent()+" 작성한 메시지가 존재합니다.");
        System.out.println("--------------------------------");

        System.out.println(" 2) 다건 조회 ");
        List<User> allUser = userService.readAll();
        allUser.stream().forEach(System.out::println);
        List<Channel> allChannel = channelService.readAll();
        allChannel.stream().forEach(System.out::println);
        List<Message> allMessage = messageService.readAll();
        allMessage.stream().forEach(System.out::println);
        System.out.println();


        System.out.println("========= [ 3. 수   정 ] =========");
        userService.update(user1.getId(),"이에은", "yeaheun123", "aaa@naver.com");
        channelService.update(channel1.getId(), ChannelType.PUBLIC, "자유게시판", "자유롭게 사용 가능~");
        messageService.update(message1.getId(), "안녕! 나는 이예은이라고해~");
        System.out.println(" 유저 수정 확인: "+user1.toString());
        System.out.println(" 채널 수정 확인: "+channel1.toString());
        System.out.println(" 메시지 수정 확인: "+message1.toString());
        System.out.println();


        System.out.println("========= [ 4. 삭   제 ] =========");
        userService.delete(user4.getId());
        channelService.delete(channel3.getId());
        messageService.delete(message3.getId());
        userService.read(user4.getId());
        channelService.read(channel3.getId());
        messageService.read(message3.getId());
        System.out.println();

        System.out.println("========= [ 5. 서비스 간 의존성 주입 ] =========");
        MessageService messageServiceDI = new JCFMessageService(userService, channelService);

        System.out.println(" 1) 정상 데이터 테스트 ");
        User realUser = userService.create("홍길동", "pw123", "hong@gmail.com");
        Channel realChannel = channelService.create(ChannelType.PUBLIC, "자바게시판", "자바 질문하는 곳");
        Message msg1 = messageServiceDI.create(realUser.getId(), realChannel.getId(), "안녕하세요! 자바 질문 있습니다.");
        if (msg1 != null) {
            System.out.println("정상 데이터 검증");
        }

        System.out.println(" 2) 유저 실패 데이터 테스트 ");
        UUID fakeUserId = UUID.randomUUID();
        Message msg2 = messageServiceDI.create(fakeUserId, realChannel.getId(), "가짜 유저 id 입니다.");
        if (msg2 == null) {
            System.out.println("가짜 유저 차단");
        }

        System.out.println(" 3) 채널 실패 데이터 테스트 ");
        UUID fakeChannelId = UUID.randomUUID();
        Message msg3 = messageServiceDI.create(realUser.getId(), fakeChannelId, "가짜 채널 id 입니다.");
        if (msg3 == null) {
            System.out.println("가짜 채널 차단");
        }
    }
}
