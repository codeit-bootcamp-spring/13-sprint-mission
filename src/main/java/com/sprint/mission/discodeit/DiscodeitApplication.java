package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.ChannelFindResponse;
import com.sprint.mission.discodeit.dto.response.UserUpdateResponse;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.jcf.JCFChannelRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFMessageRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFUserRepository;
import com.sprint.mission.discodeit.service.*;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class DiscodeitApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

		// 서비스 초기화
		UserService userService = context.getBean(UserService.class);
		ChannelService channelService = context.getBean(ChannelService.class);
		MessageService messageService = context.getBean(MessageService.class);
		BinaryContentService binaryContentService = context.getBean(BinaryContentService.class);
		UserStatusService userStatusService = context.getBean(UserStatusService.class);
		ReadStatusService readStatusService = context.getBean(ReadStatusService.class);
		AuthService authService = context.getBean(AuthService.class);


		System.out.println("\n\nThis is Discodeit Application!");

		/**
		 * 테스트 시나리오 버전 선택
		 * 원하시는 버전의 주석을 해제해주세요!
		 * 반대 버전은 주석 처리해주세요!
		 */
		System.out.println("========= 시나리오 짧은 버전 테스트 ==========\n================================");
		test(userService, channelService, messageService, binaryContentService, userStatusService, readStatusService, authService);

//        System.out.println("========= 시나리오 긴 버전 테스트 ==========\n================================");
//        testLargeCaseVersion(userService, channelService, messageService);
	}

	private static void test(
			UserService userService,
			ChannelService channelService,
			MessageService messageService,
			BinaryContentService binaryContentService,
			UserStatusService userStatusService,
			ReadStatusService readStatusService,
			AuthService authService
	) {

		//유저 객체들 생성
		System.out.println("유저 객체들 생성\n");
		User user1 = userService.createUser(new UserCreateRequest("woody", "woody@codeit.com", "qwer1234", "data/ExamplePic1.jpg"));
		User user2 = userService.createUser(new UserCreateRequest("minjae", "minjae@codeit.com", "abcd1234", "data/ExamplePic2.jpg"));
		System.out.println("==========================================");

		//UserStatus 체크
		System.out.println("UserStatus 체크\n");
		List<UserStatus> allUserStatus = userStatusService.findAllUserStatus();
		for (UserStatus userStatus : allUserStatus) {
			System.out.println("유저: " + userService.findUser(userStatus.getUserId()).name() + " 온라인 상태: " + userStatus.isUserOnline());
		}
		System.out.println("==========================================");

		//유저 로그인 후 UserStatus 다시 체크
		System.out.println("유저 로그인 후 UserStatus 다시 체크\n");
		authService.login(new LoginRequest(user1.getName(), user1.getPassword()));
		authService.login(new LoginRequest(user2.getName(), user2.getPassword()));

		allUserStatus = userStatusService.findAllUserStatus();
		for (UserStatus userStatus : allUserStatus) {
			System.out.println("유저: " + userService.findUser(userStatus.getUserId()).name() + " 온라인 상태: " + userStatus.isUserOnline());
		}
		System.out.println("==========================================");

		//채널 객체 생성
		System.out.println("채널 객체 생성\n");
		Channel privateChannel = channelService.createPrivateChannel(new PrivateChannelCreateRequest(ChannelType.PRIVATE, List.of(user1.getId(), user2.getId())));
		Channel publicChannel = channelService.createPublicChannel(new PublicChannelCreateRequest(ChannelType.PUBLIC, "CodeIt 부트캠프 채널", "CodeIt 부트캠프 채널입니다~~~~~"));
		System.out.println("==========================================");

		//user1이 privateChannel에 메세지 생성
		System.out.println("user1이 privateChannel에 메세지 생성\n");
		Message message1 = messageService.createMessage(new MessageCreateRequest("이 채널은 CodeIt 부트캠프 채널입니다.", privateChannel.getId(), user1.getId(), List.of("data/ExamplePic3.jpg", "data/ExamplePic4.jpg")));
		System.out.println("==========================================");

		//user1의 privateChannel에 대한 readStatus 체크
		System.out.println("user1의 privateChannel에 대한 readStatus 체크\n");
		List<ReadStatus> readStatusList = readStatusService.findAllReadStatusByUserId(user1.getId());
		for (ReadStatus readStatus : readStatusList) {
			System.out.println("유저: " + userService.findUser(readStatus.getUserId()).name() +"의 채널: " + channelService.findChannel(readStatus.getChannelId()).name() + "에 대한 마지막 읽음 시간: " + readStatus.getLastAccessTime());
		}

		System.out.println("==========================================");

		//user2가 privateChannel에 메세지 생성
		System.out.println("user2가 privateChannel에 메세지 생성\n");
		Message message2 = messageService.createMessage(new MessageCreateRequest("안녕하세요, 스프린터 minjae입니다!", privateChannel.getId(), user2.getId(), List.of("data/ExamplePic1.jpg", "data/ExamplePic4.jpg")));
		System.out.println("==========================================");

		//privateChannel에서 작성된 메세지 출력
		System.out.println("privateChannel에서 작성된 메세지 출력\n");
		List<Message> messageList = messageService.findAllByChannelId(privateChannel.getId());
		for (Message message : messageList) {
			System.out.println("유저: " + userService.findUser(message.getAuthorId()).name() + "\nmessage: " + message.getContent() + "\ncontent: ");
			for (UUID attachmentId : message.getAttachmentIds()) {
				System.out.println(binaryContentService.findBinaryContentById(attachmentId).getContentPath() + " ");
			}
			System.out.println();
		}
		System.out.println("==========================================");

		// user2 정보 변경
		System.out.println("user2 정보 변경\n");
		userService.updateUser(new UserUpdateRequest(user2.getId(), "박민재", "박민재@gmail.com", "MyNewPassword", null));
		System.out.println("==========================================");

		// user2가 작성한 메세지 수정
		System.out.println("user2가 작성한 메세지 수정\n");
		messageService.updateMessage(new MessageUpdateRequest(message2.getId(), "안녕하세요, 이름 바꿨습니다. 스프린터 박민재입니다.", List.of("data/ExamplePic2.jpg")));
		System.out.println("==========================================");

		//privateChannel에서 작성된 메세지 재출력
		System.out.println("privateChannel에서 작성된 메세지 재출력\n");
		messageList = messageService.findAllByChannelId(privateChannel.getId());
		for (Message message : messageList) {
			System.out.println("유저: " + userService.findUser(message.getAuthorId()).name() + "\nmessage: " + message.getContent() + "\ncontent: ");
			for (UUID attachmentId : message.getAttachmentIds()) {
				System.out.println(binaryContentService.findBinaryContentById(attachmentId).getContentPath() + " ");
			}
			System.out.println();
		}
		System.out.println("==========================================");

		//user2가 볼 수 있는 Channel들 조회
		System.out.println("user2가 볼 수 있는 Channel들 조회\n");
		List<ChannelFindResponse> responseDTO = channelService.findAllByUserId(user2.getId());
		for (ChannelFindResponse response : responseDTO) {
			System.out.print("채널타입: " + response.type() + ", 채널명: " + response.name() + ", 채널 설명: " + response.description() + ", 채널 내 유저: ");
			for (UUID userId : response.usersId()) {
				System.out.print(userService.findUser(userId).name() + " ");
			}
			System.out.println();
		}
		System.out.println("==========================================");

		// user1 삭제
		System.out.println("user1 삭제\n");
		userService.deleteUser(user1.getId());
		System.out.println("==========================================");

		//user2가 볼 수 있는 Channel들 재조회
		System.out.println("user2가 볼 수 있는 Channel들 재조회\n");
		responseDTO = channelService.findAllByUserId(user2.getId());
		for (ChannelFindResponse response : responseDTO) {
			System.out.print("채널타입: " + response.type() + ", 채널명: " + response.name() + ", 채널 설명: " + response.description() + ", 채널 내 유저: ");
			for (UUID userId : response.usersId()) {
				System.out.print(userService.findUser(userId).name() + " ");
			}
			System.out.println();
		}
		System.out.println("==========================================");

		//privateChannel에서 작성된 메세지 재출력
		System.out.println("privateChannel에서 작성된 메세지 재출력\n");
		messageList = messageService.findAllByChannelId(privateChannel.getId());
		for (Message message : messageList) {
			System.out.println("유저: " + userService.findUser(message.getAuthorId()).name() + "\nmessage: " + message.getContent() + "\ncontent: ");
			for (UUID attachmentId : message.getAttachmentIds()) {
				System.out.println(binaryContentService.findBinaryContentById(attachmentId).getContentPath() + " ");
			}
			System.out.println();
		}
		System.out.println("==========================================");

	}

}
