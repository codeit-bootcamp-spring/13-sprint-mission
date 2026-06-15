package com.sprint.mission.discodeit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
@Slf4j
public class DiscodeitApplication {

	public static void main(String[] args) {

		SpringApplication.run(DiscodeitApplication.class, args);
		log.info("서버가 실행되었습니다.");

	}


}

/* 기존 코드
ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

		// 서비스 초기화
		// TODO context에서 Bean을 조회하여 각 서비스 구현체 할당 코드 작성하세요.
		// <기존 데이터 등록>
		UserService userService = context.getBean(UserService.class);
		ChannelService channelService = context.getBean(ChannelService.class);
		MessageService messageService = context.getBean(MessageService.class);

		Scanner scanner = new Scanner(System.in); // 키보드 입력기

		UUID systemUserId = UUID.randomUUID();

		// 기존 데이터 먼저 준비
		SampleData.loadSampleNames();
		SampleData.loadChannelTitles();
		SampleData.loadMessages();

		// 기존 데이터 먼저 등록
		if (userService.findAll().isEmpty()) {
			for (String name : SampleData.names) {
				UserRequest sampleRequest = new UserRequest(
						name,
						name + "@codeit.com",
						"password123!",
						null
				);
				userService.create(sampleRequest);
			}
		}
		if (channelService.findAll(systemUserId).isEmpty()) {
			for (String title : SampleData.titles) {
				channelService.createPublicChannel(new ChannelPublicRequest(title, "샘플 설명"));
			}
		}
		if (messageService.findAllByChannelId(systemUserId).isEmpty()) {
			for (String message : SampleData.messages) {
				UUID tempChannelId = UUID.randomUUID();
				UUID tempSenderId = UUID.randomUUID();

				MessageCreateRequest sampleMessageRequest = new MessageCreateRequest(
						tempChannelId,
						tempSenderId,
						message,
						Collections.emptyList()
				);
				messageService.create(sampleMessageRequest);
			}
		}


		log.info("=== 스프링 미션 3 기본 테스트 요구사항 실행 ===");
		UserResponse testUser = setupUser(userService);
		ChannelResponse testChannel = setupChannel(channelService);
		MessageResponse testMessage = setupMessage(messageService);
		log.info("======================================\n");

		boolean running = true;

		while (running) {
			System.out.println("\n==== 메인 메뉴 ====");
			System.out.println("1. User");
			System.out.println("2. Channel");
			System.out.println("3. Message");
			System.out.println("0. 종료");
			System.out.print("선택: ");

			int choice = Integer.parseInt(scanner.nextLine()); // 숫자 입력 받기

			switch (choice) {
				case 1: UserMode.run(userService, scanner);
					break;
				case 2: ChannelMode.run(channelService, scanner);
					break;
				case 3: MessageMode.run(messageService, scanner);
					break;
				case 0: System.out.println("프로그램을 종료합니다.");
					running = false;
					break;
				default: System.out.println("잘못된 입력입니다.");
			}
		}

	}

	private static UserResponse setupUser(UserService userService) {
		UserResponse user = userService.create(new UserRequest("테스트 유저", "테스트 이메일", "테스트 패스워드", "테스트 프로필이미지"));
		log.info("-> 테스트 유저 등록 완료: {}", user.username());
		return user;
	}
	private static ChannelResponse setupChannel(ChannelService channelService) {
		ChannelResponse channel = channelService.createPublicChannel(new ChannelPublicRequest("테스트 채널", "테스트 설명"));
		log.info("-> 테스트 채널 등록 완료: {}", channel.name());
		return channel;
	}
	private static MessageResponse setupMessage(MessageService messageService) {
		UUID tempChannelId = UUID.randomUUID();
		UUID tempSenderId = UUID.randomUUID();

		MessageCreateRequest testRequest = new MessageCreateRequest(
				tempChannelId,
				tempSenderId,
				"테스트 메세지",
				Collections.emptyList()
		);

		MessageResponse message = messageService.create(testRequest);
		log.info("-> 테스트 메세지 등록 완료: {}", message.content());
		return message;
 */