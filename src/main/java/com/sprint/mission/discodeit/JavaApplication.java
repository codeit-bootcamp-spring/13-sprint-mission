package com.sprint.mission.discodeit;

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

import com.sprint.mission.discodeit.entity.ChannelType;

import java.util.UUID;


public class JavaApplication {
    public static void main(String[] args) {

        // File * Repository test line
        FileUserRepository user = new FileUserRepository();
        FileChannelRepository channel = new FileChannelRepository();
        FileMessageRepository message = new FileMessageRepository();

        // JCF * Repository test line
//        JCFUserRepository user = JCFUserRepository.open();
//        JCFChannelRepository channel = JCFChannelRepository.open();
//        JCFMessageRepository message = JCFMessageRepository.open();

        BasicChannelService chn = new BasicChannelService(channel);
        BasicUserService usr = new BasicUserService(user);
        BasicMessageService msg = new BasicMessageService(channel, user, message);

//        chn.createChannel("test2","this is test2",ChannelType.PUBLIC);
//        System.out.println(chn.getChannelList());
//
//        usr.createUser("user2","user2", "password");
//        System.out.println(usr.getUserList());

//        msg.createMessage(UUID.fromString("7c4262cb-ff43-4452-b395-22b5db2a37f5"),
//                UUID.fromString("ac4f3a38-ff96-4ebc-82ad-3021f14ab12a"),
//                "this is msg data.");
        System.out.println(msg.getMessageList());
        msg.deleteMessage(UUID.fromString("8838a6c6-29fa-466a-8fd2-009315d1e629"));
        System.out.println(msg.getMessageList());

    }
}


