package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.input.BinaryContentInput;
import com.sprint.mission.discodeit.dto.input.CreateMessageInput;
import com.sprint.mission.discodeit.dto.input.Login;
import com.sprint.mission.discodeit.dto.input.UserProfile;
import com.sprint.mission.discodeit.dto.output.ChannelOutput;
import com.sprint.mission.discodeit.dto.output.UserOutput;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;


@Component
@Slf4j
public class DiscodeitInit implements CommandLineRunner {
    ChannelService cs;
    UserService us;
    MessageService ms;
    AuthService auth;
    ReadStatusService rss;
    UserStatusService uss;
    BinaryContentService bcs;


    public DiscodeitInit(
            ChannelService cs,
            UserService us,
            MessageService ms,
            AuthService auth,
            ReadStatusService rss,
            UserStatusService uss,
            BinaryContentService bcs
            ) {
        this.cs = cs;
        this.us = us;
        this.ms = ms;
        this.auth = auth;
        this.rss = rss;
        this.uss = uss;
        this.bcs = bcs;
    }

    // Todo - 장기간 디버깅 시, 메모리 부족 에러 원인 찾기
    // Todo - msg : Streaming write failed writing file a target machine: NotEnoughSpace(where=/tmp/EX0ARX/gradle-api-9.3.0.jar.part, message=No space left)
    // Todo - 클래스 버전 변경 이유 찾기.

    @Override
    public void run(String... args) throws Exception {
        // test
        // 리소스 준비
        try {
            us.createUser(
                    new Login("test@email.com","password"),
                    new UserProfile("김철수",null)
            );
            UserOutput cuser = auth.login(new Login("test@email.com","password"));
            log.info("{}",cuser);

            cs.createPrivateChannel(cuser.getId());
            log.info("channel created");

            List<ChannelOutput> cnls = cs.findAllByUserID(cuser.getId());
            log.info("{}", cnls.get(0));

            List<ReadStatus> rsss = rss.findAllByUserID(cuser.getId());
            log.info("{}",rsss);

            // file upload mock
            BinaryContent bc = bcs.create(BinaryContentInput.builder()
                    .authorID(cuser.getId())
                    .contentID(UUID.randomUUID())
                    .build());

            log.info("{}",bcs.find(bc.getId()));


            ms.createMessage(CreateMessageInput.builder()
                    .userID(cuser.getId())
                    .channelID(cnls.get(0).getChannelID())
                    .dataIDs(Set.of(bc.getId()))
                    .message("this is test msg")
                    .build());

            List<Message> msgs = ms.findallByChannelId(cnls.get(0).getChannelID());
            log.info("{}",msgs);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

            UserOutput cuser = auth.login(new Login("test@email.com","password"));
            log.info("{}",cuser);

            List<ChannelOutput> cnls = cs.findAllByUserID(cuser.getId());

            for (ChannelOutput c : cnls){
                log.info("{}",c);
                cs.deleteChannel(c.getChannelID());
            }

            us.deleteUser(cuser.getId());
            log.info("user deleted");
        }
    }
}
