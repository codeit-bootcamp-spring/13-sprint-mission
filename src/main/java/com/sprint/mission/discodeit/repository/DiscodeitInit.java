package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DiscodeitInit implements CommandLineRunner {
    BasicChannelService chn;
    BasicUserService usr;
    BasicMessageService msg;

    public DiscodeitInit(BasicChannelService chn, BasicUserService usr, BasicMessageService msg) {
        this.chn = chn;
        this.usr = usr;
        this.msg = msg;
    }
    @Override
    public void run(String... args) throws Exception {
        //        chn.createChannel("test2","this is test2",ChannelType.PUBLIC);
        System.out.println(chn.getChannelList());
//
//        usr.createUser("user2","user2", "password");
        System.out.println(usr.getUserList());

//        msg.createMessage(UUID.fromString("7c4262cb-ff43-4452-b395-22b5db2a37f5"),
//                UUID.fromString("ac4f3a38-ff96-4ebc-82ad-3021f14ab12a"),
//                "this is msg data.");
        System.out.println(msg.getMessageList());
//        msg.deleteMessage(UUID.fromString("8838a6c6-29fa-466a-8fd2-009315d1e629"));
//        System.out.println(msg.getMessageList());
    }
}
