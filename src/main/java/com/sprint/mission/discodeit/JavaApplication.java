package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.repository.file.*;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import java.util.List;
import java.util.UUID;

public class JavaApplication {
    // 셋업 추가
    static UserResponse setupUser(UserService userService) {
        return userService.create(new UserCreateRequest("정성찬", "sungchan@icloud.com", "DcdSungchan234?!", null));
    }
    static ChannelResponse setupChannel(ChannelService channelService) {
        return channelService.createPublicChannel(new PublicChannelCreateRequest("스터디", "코딩테스트 스터디 모집합니다."));
    }
    static void messageCreateTest(MessageService messageService, ChannelResponse channel, UserResponse user) {
        MessageResponse message=messageService.create(new MessageCreateRequest("남은 시간도 화이팅입니다~!", channel.id(), user.id(), null));
        System.out.println("=== 메시지 생성: "+message.id());
    }

    // 테스트 도메인별 분리
    static void userCRUDTest(UserService userService) {
        // 생성
        UUID profileId=UUID.randomUUID();
        UserResponse user=userService.create(new UserCreateRequest("박원빈", "wonbin@icloud.com", "Dcdwonbin38?!", null));
        System.out.println("=== 사용자 계정 생성: "+user.id());
        // 조회 (단건, 다건) check
        System.out.println("=== 사용자 조회 ===");
        UserResponse foundUser =userService.find(user.id());
        System.out.println("사용자 조회--단건: "+ String.join("/", foundUser.id().toString(),
                foundUser.username(), foundUser.email(), foundUser.profileId()==null? "없음":foundUser.profileId().toString()));
        List<UserResponse> foundUsers =userService.findAll();
        System.out.println("사용자 조회--다건: (사용자 수: "+ foundUsers.size()+")");
        foundUsers.forEach(userResponse -> System.out.println(String.join("/", userResponse.id().toString(),
                userResponse.username(), userResponse.email(), userResponse.profileId()==null? "없음" : userResponse.profileId().toString())));
        // 수정 edit
        // 수정된 데이터 조회
        System.out.println("=== 프로필 수정 ====");
        UserResponse updatedUser = userService.update(new UserUpdateRequest(user.id(), "구정모", "jungmo0205@discodeit.com", "DscdJungmo234!!", null));
        System.out.println("수정된 사용자: "+String.join("/", updatedUser.username(), updatedUser.email()));
        // 삭제 delete
        // 조회 통해 삭제되었는지 확인
        System.out.println("=== 사용자 삭제 ====");
        userService.delete(user.id());
        List<UserResponse> foundDeletedUsers=userService.findAll();
        System.out.println("계정 삭제 완료! 현재 사용자 수: "+foundDeletedUsers.size());
    }

    static void channelCRUDTest(ChannelService channelService, UserResponse user) {
        ChannelResponse channel=channelService.createPublicChannel(new PublicChannelCreateRequest("학습", "개발-자료 채널의 시작이에요."));
        System.out.println("=== 채널 생성: "+channel.id());
        System.out.println("=== 채널 조회 ===");
        ChannelResponse foundChannel =channelService.find(channel.id());
        System.out.println("채널 조회--단건"+ String.join("/", foundChannel.id().toString(),
                foundChannel.type().toString(), foundChannel.name(), foundChannel.description()));
        List<ChannelResponse> foundChannels =channelService.findAllByUserId(user.id());
        System.out.println("채널 조회--다건 (채널 수: "+ foundChannels.size()+")");
        foundChannels.forEach(channelResponse -> System.out.println(String.join("/", channelResponse.type().toString(),
                channelResponse.name(), channelResponse.description())));
        System.out.println("=== 채널 수정 ====");
        ChannelResponse updatedChannel = channelService.update(new ChannelUpdateRequest(channel.id(), ChannelType.PRIVATE, "학습-문의", "학습 질의응답을 위한 채널이에요."));
        System.out.println("수정된 채널: "+String.join("/", updatedChannel.type().toString(),
                updatedChannel.name(), updatedChannel.description()));
        System.out.println("=== 채널 삭제 ====");
        channelService.delete(channel.id());
        List<ChannelResponse> foundDeletedChannels=channelService.findAllByUserId(user.id());
        System.out.println("채널 삭제 완료! 현재 채널 수: "+foundDeletedChannels.size());
    }

    static void messageCRUDTest(MessageService messageService, ChannelResponse channel, UserResponse user) {
        UUID channelId=UUID.randomUUID();
        UUID authorId=UUID.randomUUID();
        MessageResponse message=messageService.create(new MessageCreateRequest("ZEP 로그 기록 확인 후 연락드렸습니다.", channel.id(), user.id(), List.of()));
        System.out.println("=== 메시지 생성: "+message.id());
        MessageResponse foundMessage =messageService.find(message.id());
        System.out.println("메시지 조회--단건: "+String.join("/",foundMessage.id().toString(),
                foundMessage.content(), foundMessage.channelId().toString(), foundMessage.authorId().toString(), foundMessage.attachmentIds().toString()));
        List<MessageResponse> readMessages=messageService.findallByChannelId(channel.id());
        System.out.println("메시지 조회--다건 (메시지 수: "+readMessages.size()+")");
        readMessages.forEach(messageResponse -> System.out.println(String.join("/", messageResponse.id().toString(),
                messageResponse.content(), messageResponse.channelId().toString(), messageResponse.authorId().toString(), messageResponse.attachmentIds().toString())));
        System.out.println("=== 메시지 수정 ====");
        MessageResponse updateMessage=messageService.update(new MessageUpdateRequest(message.id(), "회원님의 활동을 응원합니다."));
        System.out.println("=== 수정된 메시지: "+updateMessage.content());
        System.out.println("=== 메시지 삭제 ====");
        messageService.delete(message.id());
        List<MessageResponse> foundDeletedMessage=messageService.findallByChannelId(channel.id());
        System.out.println("메시지 삭제! 현재 메시지 수: "+foundDeletedMessage.size());
    }

    public static void main(String[] args) {
        // 레포지토리 초기화
        UserRepository userRepository=new FileUserRepository();
        MessageRepository messageRepository=new FileMessageRepository();
        ChannelRepository channelRepository=new FileChannelRepository();
        BinaryContentRepository binaryContentRepository=new FileBinaryContentRepository();
        ReadStatusRepository readStatusRepository=new FileReadStatusRepository();
        UserStatusRepository userStatusRepository=new FileUserStatusRepository();
        // 서비스 초기화
        // 다형성 활용해 Service 구현체가 늘어나더라도 다른 구현체와의 교체가 쉽도록 함
        UserService userService=new BasicUserService(userRepository, binaryContentRepository, userStatusRepository);
        ChannelService channelService=new BasicChannelService(channelRepository, readStatusRepository, messageRepository);
        MessageService messageService=new BasicMessageService(messageRepository, channelRepository, userRepository, binaryContentRepository);
        // 셋업
        BinaryContent binaryContent;
        UserResponse user=setupUser(userService);
        ChannelResponse channel=setupChannel(channelService);
        // 테스트
        userCRUDTest(userService);
        channelCRUDTest(channelService, user);
        messageCRUDTest(messageService, channel, user);
        messageCreateTest(messageService, channel, user);
    }
}
