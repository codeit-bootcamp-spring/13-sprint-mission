package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import com.sprint.mission.discodeit.service.file.FileChannelService;
import com.sprint.mission.discodeit.service.file.FileMessageService;
import com.sprint.mission.discodeit.service.file.FileUserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DiscodeitApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

		System.out.println("============================ FileUserService 테스트 ============================");

		UserService fileUserService = context.getBean(FileUserService.class);

		// 생성
		User user1 = fileUserService.create("영경",
				"young@gmail.com",
				"3333@@");
		User user2 = fileUserService.create("기요미다난", "giyomi@gmail.com", "1234@@");

		// 단일 조회
		System.out.println(fileUserService.find(user1.getId()));

		// 전체 조회
		System.out.println(fileUserService.findAll());

		// 수정 후 조회
		fileUserService.update(
				user1.getId(),
				"김영경",
				"updated@gmail.com",
				"4444@@"
		);
		System.out.println(fileUserService.find(user1.getId()));

		// 삭제 후 조회
		fileUserService.delete(user1.getId());
		System.out.println(fileUserService.findAll());

		System.out.println("============================ FileChannelService 테스트 ============================");

		ChannelService fileChannelService = context.getBean(FileChannelService.class);

		// 생성
		Channel channel1 = fileChannelService.create(
				"점메추 모임",
				Channel.ChannelType.PRIVATE,
				"점메추 활발히 참여해주세요."
		);
		Channel channel2 = fileChannelService.create(
				"저메추 모임",
				Channel.ChannelType.PRIVATE,
				"저메추 활발히 참여해주세요."
		);

		// 단일 조회
		System.out.println(fileChannelService.find(channel1.getId()));

		// 전체 조회
		System.out.println(fileChannelService.findAll());

		// 수정 후 조회
		fileChannelService.update(
				channel1.getId(),
				"점심 메뉴 추천 모임",
				Channel.ChannelType.PRIVATE,
				"맛집만 알려주셔야 해요."
		);
		System.out.println(fileChannelService.find(channel1.getId()));

		// 삭제 후 조회
		fileChannelService.delete(channel1.getId());
		System.out.println(fileChannelService.findAll());

		System.out.println("============================ FileMessageService 테스트 ============================");

		MessageService fileMessageService = context.getBean(FileMessageService.class);

		// 생성
		Message message1 = fileMessageService.create(
									"떡뽀끼 어때요",
											channel2.getId(),
											user2.getId());

		// 단일 조회
		System.out.println(fileMessageService.find(message1.getId()));

		// 전체 조회
		System.out.println(fileMessageService.findAll());

		// 수정 후 조회
		fileMessageService.update(
				message1.getId(),
				"연어덮밥 마싯겠다"
		);
		System.out.println(fileMessageService.find(message1.getId()));

		// 삭제 후 조회
		fileMessageService.delete(message1.getId());
		System.out.println(fileMessageService.findAll());

	}

}
