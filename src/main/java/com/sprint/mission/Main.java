package com.sprint.mission;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import java.util.List;
import java.util.Scanner;

//TIP 코드를 <b>실행</b>하려면 <shortcut actionId="Run"/>을(를) 누르거나
// 에디터 여백에 있는 <icon src="AllIcons.Actions.Execute"/> 아이콘을 클릭하세요.
public class Main {
    public static class JavaApplication {
        public static void main(String[] args) {
            //TIP 캐럿을 강조 표시된 텍스트에 놓고 <shortcut actionId="ShowIntentionActions"/>을(를) 누르면
            // IntelliJ IDEA이(가) 수정을 제안하는 것을 확인할 수 있습니다.
            UserService userService = new JCFUserService();
            ChannelService channelService = new JCFChannelService();
            MessageService messageService = new JCFMessageService();
            Scanner scanner = new Scanner(System.in);

            //User 테스트
            //등록
            System.out.print("등록할 사용자 이름을 입력하세요: ");
            String userName = scanner.nextLine();
            User user = new User(userName);
            userService.create(user);

            //단건조회
            User foundUser = userService.read(user.getId());
            System.out.println("조회한 사용자 이름: " + foundUser.getName());

            //전체조회
            List<User> users = userService.readAll();
            System.out.println("전체 사용자 수: " + users.size());

            //수정
            System.out.print("수정할 사용자 이름을 적어주세요: ");
            String newUserName = scanner.nextLine();
            user.updateName(newUserName);
            userService.update(user);

            //수정된 데이터 조회
            User updatedUser = userService.read(user.getId());
            System.out.println("수정 후 사용자 이름: " + updatedUser.getName());

            //삭제
            userService.delete(user.getId());

            //삭제 후 조회
            User deletedUser = userService.read(user.getId());
            if (deletedUser == null) {
                System.out.println("사용자가 성공적으로 삭제되었습니다.");
            }


            // CHannel테스트
            //등록
            System.out.print("등록할 채널명을 입력하세요: ");
            String channelName = scanner.nextLine();
            Channel channel = new Channel(channelName);
            channelService.create(channel);

            //단건조회
            Channel foundChannel = channelService.read(channel.getId());
            System.out.println("조회한 채널명: " + foundChannel.getName());

            //전체조회
            List<Channel> channels = channelService.readAll();
            System.out.println("전체 채널 수: " + channels.size());

            //수정
            System.out.print("수정할 채널명을 적어주세요: ");
            String newChannelName = scanner.nextLine();
            channel.updateName(newChannelName);
            channelService.update(channel);

            //수정된 데이터 조회
            Channel updatedchannel = channelService.read(channel.getId());
            System.out.println("수정 후 채널 이름: " + updatedchannel.getName());

            //삭제
            channelService.delete(channel.getId());

            //삭제 후 조회
            Channel deletedChannel = channelService.read(channel.getId());
            if (deletedChannel == null) {
                System.out.println("채널이 성공적으로 삭제되었습니다.");
            }


            //Message테스트
            //등록
            System.out.print("메세지 보낼 사용자이름을 입력해주세요: ");
            String messageUserName = scanner.nextLine();
            User messageUser = new User(messageUserName);
            userService.create(messageUser);

            System.out.print("메세지 보낼 채널명을 입력해주세요: ");
            String messageChannelName = scanner.nextLine();
            Channel messageChannel = new Channel(messageChannelName);
            channelService.create(messageChannel);

            System.out.print("등록할 메시지 내용을 입력해주세요: ");
            String messageContent = scanner.nextLine();
            Message message = new Message(messageContent, messageUser.getId(), messageChannel.getId());
            messageService.create(message);

            //단건조회
            Message foundMessage = messageService.read(message.getId());
            System.out.println("조회한 메세지 이름: " + foundMessage.getContent());

            //전체조회
            List<Message> messages = messageService.readAll();
            System.out.println("전체 메세지 수: " + messages.size());

            //수정
            System.out.print("수정할 메세지 내용을 적어주세요: ");
            String newMessageContent = scanner.nextLine();
            message.updateContent(newMessageContent);
            messageService.update(message);

            //수정된 데이터 조회
            Message updatedMessage = messageService.read(message.getId());
            System.out.println("수정 후 메세지 이름: " + updatedMessage.getContent());

            //삭제
            messageService.delete(message.getId());

            //삭제 후 조회
            Message deletedMessage = messageService.read(message.getId());
            if (deletedMessage == null) {
                System.out.println("메세지가 성공적으로 삭제되었습니다.");
            }

        }
    }
}