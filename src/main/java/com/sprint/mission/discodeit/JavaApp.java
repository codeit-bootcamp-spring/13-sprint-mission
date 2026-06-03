package com.sprint.mission.discodeit;



import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFChannelRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFMessageRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;


import java.util.Collection;

public class JavaApp {
	public static void main(String[] args) {

		UserRepository userRepository = new FileUserRepository(); //new JCFUserRepository();
		ChannelRepository channelRepository = new FileChannelRepository(); //JCFChannelRepository();
		MessageRepository messageRepository = new FileMessageRepository(); //JCFMessageRepository();

		UserService userService = new BasicUserService(userRepository);
		ChannelService channelService = new BasicChannelService(channelRepository);
		MessageService messageService = new BasicMessageService(messageRepository, userRepository, channelRepository);



		
		// 유저 생성
		User user1 = new User("KJH", "KJH@kjh.com");
		User user2 = new User("UHJ", "UHJ@kjh.com");
		User user3 = new User("PMJ", "PMJ@kjh.com");
		User user4 = new User("JHS", "JHS@kjh.com");
		User user5 = new User("LHB", "LHB@kjh.com");
		
		userService.create(user1);
		userService.create(user2);
		userService.create(user3);
		userService.create(user4);
		userService.create(user5);
		
		
		System.out.println("\n============== 유저 생성 확인 ==================");
		System.out.println("All of members: " + userService.findAll().size());
		
		
		System.out.println("\n============== 유저 조회 (단건)) ==================");
		User found = userService.findById(user1.getId());
		if (found != null) {
			System.out.println("Found user: " + found.getName());
			System.out.println("Found id: " + found.getId()+ "\n");
		}
		
		
		System.out.println("\n============== 유저 조회 (다건)) ==================");
		Collection <User> allUsers = userService.findAll();
		for (User u : allUsers) {
			System.out.println("ID: " + u.getId() +
					" | 이름: " + u.getName() + " | 이메일: " + u.getEmail());
		}
		
		
		System.out.println("\n============== 유저 수정 ==================");
		userService.update(user1.getId(), "LKM", "LKM@kjh.com");
		User updatedUser = userService.findById(user1.getId());
		
		System.out.println("updated id: " + updatedUser.getId());
		System.out.println("Updated user: " + updatedUser.getName());
		System.out.println("Updated email: " + updatedUser.getEmail());
		System.out.println("Created Time: " + found.getCreateAt());
		System.out.println("Updated Time: " + found.getUpdateAt());
		
		
		System.out.println("\n============== 유저 삭제 및 조회==================");
		userService.delete(user1.getId());
		User deletedUser = userService.findById(user1.getId());
		System.out.println("Deleted user id: " + user1.getId());
		System.out.println("Deleted user name: " + user1.getName());
		System.out.println("Deleted user email: " + user1.getEmail());
		System.out.println("Deleted user updateTime: " + user1.getUpdateAt());
		System.out.println("All of members: " + userService.findAll().size());
		
		
		//채널
		System.out.println("\n============== 채널 생성 ==================");
		Channel channel1 = new Channel("Codeit" , "Sprint Boot Camp",
				user5, Channel.channelType.TEXT);
		Channel channel2 = new Channel("Codeit2" , "Sprint Boot Camp",
				user4, Channel.channelType.TEXT);
		channelService.create(channel1);
		channelService.create(channel2);
		System.out.println("All of channels: " + channelService.findAll().size());
		System.out.println("Channels: " + channelService.findById(channel1.getId()));
		
		
		System.out.println("\n============== 채널 조회 (단건)) ==================");
		Channel found1 = channelService.findById(channel1.getId());
		if (found1 != null) {
			System.out.println("Found Channel: " + found1.getName());
			System.out.println("Found channel id: " + found1.getId());
			System.out.println("Found channel created By: " + found1.getCreator().getName());
			System.out.println("Found channel description: " + found1.getNameDescription()+ "\n");
		}
		
		System.out.println("\n============== 채널 조회 (다건)) ==================");
		Collection <Channel> allChannels = channelService.findAll();
		for (Channel channel : allChannels) {
			System.out.println("ID: " + channel.getId() +
					" | 이름: " + channel.getName() + " | 채널 설명: " + channel.getNameDescription());
		}
		
		System.out.println("\n============== 채널 수정 ==================");
		channelService.update(channel1.getId(), "Laptop", "MacbookPro M1");
		Channel updatedchannel = channelService.findById(channel1.getId());
		System.out.println("Updated channel id: " + channel1.getId());
		System.out.println("Updated channel name: " + channel1.getName());
		System.out.println("Updated channel description: " + channel1.getNameDescription());
		System.out.println("Created Time: " + found1.getCreateAt());
		System.out.println("Updated Time: " + found1.getUpdateAt());
		
		
		System.out.println("\n============== 채널 삭제 및 조회 ==================");
		channelService.delete(channel1.getId());
		Channel deletedchannel = channelService.findById(channel1.getId());
		System.out.println("Deleted channel id: " + channel1.getId());
		System.out.println("Deleted channel name: " + channel1.getName());
		System.out.println("Deleted channel description: " + channel1.getNameDescription());
		System.out.println("Deleted channel updateTime: " + channel1.getUpdateAt());
		System.out.println("All of channels: " + channelService.findAll().size());
		
		//메세지
		System.out.println("\n============== 메세지 생성 ==================");
		Message message1 = new Message("How to feels today?" , user2, channel2);
		Message message2 = new Message("I'm bad, b/c I was car lol" , user3, channel2);
		Message message3 = new Message("So nice, toady." , user4, channel2);
		Message message4 = new Message("It's good. HRU?" , user5, channel2);
		Message message5 = new Message("User2, that's to bad." , user2, channel2);
		
		messageService.create(message1);
		messageService.create(message2);
		messageService.create(message3);
		messageService.create(message4);
		messageService.create(message5);
		
		System.out.println("All of channels: " + messageService.findAll().size());
		
		System.out.println("\n============== 메세지 조회 (단건)) ==================");
		Message messageFound = messageService.findById(message1.getId());
			System.out.println(messageFound);
		
		System.out.println("\n============== 메세지 조회 (다건)) ==================");
		Collection <Message> allMessages = messageService.findAll();
		for (Message messages : allMessages) {
			System.out.println(messages);
		}
		
		System.out.println("\n============== 메세지 삭제 및 조회 ==================");
		messageService.delete(message2.getId());
		Message deletedmessage = messageService.findById(message2.getId());
		System.out.println(deletedmessage); {
			if (deletedmessage != null) {
				System.out.println("찾은 메세지 : " + deletedmessage);
			} else {
				System.out.println("더 이상 메세지를 찾을 수 없습니다.");
			};
		};
		System.out.println("All of messages: " + messageService.findAll().size());
		
		System.out.println("\n============== 가짜 유저 검증 테스트 ==============");
		
		User fakeUser = new User("해커", "hacker@email.com");
		Channel channel = channel2;
		
		try {
			Message hackMessage = new Message("치트키 씁니다", fakeUser, channel2);
			messageService.create(hackMessage);
			
			System.out.println("검증 실패: 가짜 유저인데 저장되었습니다.");
		} catch (IllegalArgumentException e) {
			System.out.println("검증 성공! 에러 메시지: " + e.getMessage());
		}
		
		
	}
	
	
	
	
	
}
