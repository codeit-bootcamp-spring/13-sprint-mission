package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateByUserIdRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.dto.response.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.dto.response.UserStatusResponse;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.ReadStatusService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@SpringBootApplication
public class DiscodeitApplication {

    static UserResponse setupUser(UserService userService) {
        return userService.create(new UserCreateRequest(
                "HamJi",
                "hamji@codeit.com",
                "qwerty1234",
                null
        ));
    }

    static ChannelResponse setupChannel(ChannelService channelService) {
        return channelService.createPublic(new PublicChannelCreateRequest(
                "공지",
                "공지 채널입니다."
        ));
    }

    static void messageCreateTest(MessageService messageService, ChannelResponse channel, UserResponse author) {
        MessageResponse message = messageService.create(new MessageCreateRequest(
                "안녕하세요.",
                channel.id(),
                author.id(),
                null
        ));

        System.out.println("메시지 생성: " + message.id());
    }

    private static void crud(
            UserService userService,
            ChannelService channelService,
            MessageService messageService,
            AuthService authService,
            BinaryContentService binaryContentService,
            ReadStatusService readStatusService,
            UserStatusService userStatusService
    ) {
        UserResponse user1 = userService.create(new UserCreateRequest(
                "user1",
                "user1@codeit.com",
                "password1",
                new BinaryContentCreateRequest(
                        "profile.txt",
                        7L,
                        "text/plain",
                        "profile".getBytes(StandardCharsets.UTF_8)
                )
        ));

        UserResponse user2 = userService.create(new UserCreateRequest(
                "user2",
                "user2@codeit.com",
                "password2",
                null
        ));

        UserResponse user3 = userService.create(new UserCreateRequest(
                "user3",
                "user3@codeit.com",
                "password3",
                null
        ));

        System.out.println("[유저 생성] " + user1.id());
        System.out.println("[유저 단건 조회] " + userService.find(user1.id()));
        System.out.println("[유저 전체 조회] " + userService.findAll().size());
        UserResponse updatedUser = userService.update(new UserUpdateRequest(
                user1.id(),
                "newUser1",
                null,
                "newPassword1",
                null
        ));
        System.out.println("[유저 업데이트] " + updatedUser.username());

        UserResponse loggedIn = authService.login(new LoginRequest(
                updatedUser.username(),
                "newPassword1"
        ));
        System.out.println("[계정 로그인] " + loggedIn.id());

        BinaryContentResponse binaryContent = binaryContentService.create(new BinaryContentCreateRequest(
                "sample.txt",
                5L,
                "text/plain",
                "hello".getBytes(StandardCharsets.UTF_8)
        ));
        System.out.println("[바이너리 컨텐츠 생성] " + binaryContent.id());
        System.out.println("[바이너리 컨텐츠 단건 조회] " + binaryContentService.find(binaryContent.id()).fileName());
        System.out.println("[바이너리 컨텐츠 ID 목록 조회] "
                + binaryContentService.findAllByIdIn(List.of(binaryContent.id())).size());

        ChannelResponse publicChannel = channelService.createPublic(new PublicChannelCreateRequest(
                "공개",
                "공개 채널입니다."
        ));
        System.out.println("[공개 채널 생성] " + publicChannel.id());
        System.out.println("[채널 조회] " + channelService.find(publicChannel.id()).id());
        System.out.println("[채널 ID 목록 조회] " + channelService.findAllByUserId(user1.id()).size());
        ChannelResponse updatedChannel = channelService.update(new ChannelUpdateRequest(
                publicChannel.id(),
                "공개 채널",
                "채널 공개입니다."
        ));
        System.out.println("[채널 업데이트] " + updatedChannel.name());

        ChannelResponse privateChannel = channelService.createPrivate(new PrivateChannelCreateRequest(
                List.of(user1.id(), user2.id())
        ));
        System.out.println("[비공개 채널 생성] " + privateChannel.participantIds().size());

        MessageResponse message = messageService.create(new MessageCreateRequest(
                "소시지",
                updatedChannel.id(),
                user1.id(),
                List.of(new BinaryContentCreateRequest(
                        "attach.txt",
                        10L,
                        "text/plain",
                        "attachment".getBytes(StandardCharsets.UTF_8)
                ))
        ));
        System.out.println("[메시지 생성] " + message.id());
        System.out.println("[메시지 조회] " + messageService.find(message.id()).content());
        System.out.println("[메시지 ID 목록 조회] "
                + messageService.findAllByChannelId(updatedChannel.id()).size());
        MessageResponse updatedMessage = messageService.update(new MessageUpdateRequest(
                message.id(),
                "메시지 추가 입니다."
        ));
        System.out.println("[메시지 업데이트] " + updatedMessage.content());
        messageService.delete(updatedMessage.id());
        System.out.println("[메시지 삭제] " + updatedMessage.id());

        ReadStatusResponse readStatus = readStatusService.create(new ReadStatusCreateRequest(
                user2.id(),
                updatedChannel.id(),
                Instant.now()
        ));
        System.out.println("[읽음 상태 생성] " + readStatus.id());
        System.out.println("[읽음 상태 조회] " + readStatusService.find(readStatus.id()).id());
        System.out.println("[읽음 상태 ID 목록 조회] "
                + readStatusService.findAllByUserId(user2.id()).size());
        ReadStatusResponse updatedReadStatus = readStatusService.update(new ReadStatusUpdateRequest(
                readStatus.id(),
                Instant.now()
        ));
        System.out.println("[읽음 상태 업데이트] " + updatedReadStatus.lastReadAt());
        readStatusService.delete(updatedReadStatus.id());
        System.out.println("[읽음 상태 삭제] " + updatedReadStatus.id());

        UserStatusResponse existingStatus = userStatusService.findAll().stream()
                .filter(status -> status.userId().equals(user3.id()))
                .findFirst()
                .orElseThrow();
        userStatusService.delete(existingStatus.id());
        UserStatusResponse userStatus = userStatusService.create(new UserStatusCreateRequest(
                user3.id(),
                Instant.now()
        ));
        System.out.println("[유저 상태 생성] " + userStatus.id());
        System.out.println("[유저 상태 단건 조회] " + userStatusService.find(userStatus.id()).id());
        System.out.println("[유저 상태 전체 조회] " + userStatusService.findAll().size());
        UserStatusResponse updatedUserStatus = userStatusService.update(new UserStatusUpdateRequest(
                userStatus.id(),
                Instant.now()
        ));
        System.out.println("[유저 상태 업데이트] " + updatedUserStatus.lastActiveAt());
        UserStatusResponse updatedByUserId = userStatusService.updateByUserId(
                new UserStatusUpdateByUserIdRequest(user3.id(), Instant.now())
        );
        System.out.println("[유저 ID로 상태 변경] " + updatedByUserId.userId());
        userStatusService.delete(updatedByUserId.id());
        System.out.println("[유저 상태 삭제] " + updatedByUserId.id());

        binaryContentService.delete(binaryContent.id());
        System.out.println("[바이너리 컨텐츠 삭제] " + binaryContent.id());

        channelService.delete(privateChannel.id());
        channelService.delete(updatedChannel.id());
        userService.delete(user1.id());
        userService.delete(user2.id());
        userService.delete(user3.id());
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context
                = SpringApplication.run(DiscodeitApplication.class, args);

        UserService userService = context.getBean(UserService.class);
        ChannelService channelService = context.getBean(ChannelService.class);
        MessageService messageService = context.getBean(MessageService.class);
        AuthService authService = context.getBean(AuthService.class);
        BinaryContentService binaryContentService = context.getBean(BinaryContentService.class);
        ReadStatusService readStatusService = context.getBean(ReadStatusService.class);
        UserStatusService userStatusService = context.getBean(UserStatusService.class);

        try {
            crud(
                    userService,
                    channelService,
                    messageService,
                    authService,
                    binaryContentService,
                    readStatusService,
                    userStatusService
            );
        } finally {
            context.close();
        }
    }
}
