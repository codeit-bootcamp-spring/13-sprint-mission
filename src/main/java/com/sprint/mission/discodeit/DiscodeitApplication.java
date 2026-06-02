package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DiscodeitApplication {

	//user, channel, content 생성 테스트
	static User setupUser(UserService userService) {
		return userService.createUser("박경석", "aaa@gmail.com", "1234");
	}
	static Channel setupChannel(ChannelService channelService) {
		return channelService.createChannel("소개채널", "본인을 소개 해주세요!");
	}
	static void messageCreateTest(MessageService messageService, Channel channel, User author) {
		Message message = messageService.createContent("안녕하세요!", channel.getChannelId(), author.getUserId());
	}

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

		UserService userService = context.getBean(UserService.class);
		ChannelService channelService = context.getBean(ChannelService.class);
		MessageService messageService = context.getBean(MessageService.class);

		User user = setupUser(userService);
		Channel channel = setupChannel(channelService);
		messageCreateTest(messageService, channel, user);
	}
}
