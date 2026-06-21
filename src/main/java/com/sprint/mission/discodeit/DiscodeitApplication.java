package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import static com.sprint.mission.discodeit.JavaApplication.*;

@SpringBootApplication
public class DiscodeitApplication {
	// 셋업 추가
	static UserResponse setupUser(UserService userService) {
        return userService.create(new UserCreateRequest("오시온", "sion@icloud.com", "DcodeSion?!", null));
	}
	static ChannelResponse setupChannel(ChannelService channelService) {
        return channelService.createPublicChannel(new PublicChannelCreateRequest("학습", "학습 관련 공지입니다."));
	}
	static void messageCreateTest(MessageService messageService, ChannelResponse channel, UserResponse user) {
		MessageResponse message=messageService.create(new MessageCreateRequest("클래스 매니저님과 나눈 다이렉트 메시지의 첫 부분이에요.", channel.getId(), user.getId(), null));
		System.out.println("=== 메시지 생성: "+message.getId());
	}
	public static void main(String[] args) {
		ConfigurableApplicationContext context=SpringApplication.run(DiscodeitApplication.class, args);
		// 서비스 초기화
		// context에서 Bean을 조회해 각 서비스 구현체 할당 코드 작성
		UserService userService=context.getBean(UserService.class);
		ChannelService channelService=context.getBean(ChannelService.class);
		MessageService messageService=context.getBean(MessageService.class);

		// 우리가 만든 bean이 컨테이너에 등록되었는지 확인
		System.out.println("=== 패키지의 Bean 목록 ===");
		String[] allBeanNames= context.getBeanDefinitionNames();
		// 등록된 bean들의 이름을 출력
		for (String name : allBeanNames) {
			Object bean=context.getBean(name);
			String beanClassName=bean.getClass().getName();
			if (beanClassName.startsWith("com.sprint.mission.discodeit")) {
				System.out.println("•"+name+"→"+beanClassName);
			}
		}
		// DI가 동작하는지 - Repository에 활동 추가 후 출력
		System.out.println("=== 샘플 활동 추가 ===");
		// 셋업 추가
		UserResponse user=setupUser(userService);
		ChannelResponse channel=setupChannel(channelService);
		messageCreateTest(messageService, channel, user);
		// 테스트 추가
		userCRUDTest(userService);
		channelCRUDTest(channelService, user);
		messageCRUDTest(messageService, channel, user);
	}
}