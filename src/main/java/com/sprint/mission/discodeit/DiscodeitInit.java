package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.config.DiscodeitConfig;

import com.sprint.mission.discodeit.service.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


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

    DiscodeitConfig dci;


    public DiscodeitInit(
            ChannelService cs,
            UserService us,
            MessageService ms,
            AuthService auth,
            ReadStatusService rss,
            UserStatusService uss,
            BinaryContentService bcs,
            DiscodeitConfig dci
            ) {
        this.cs = cs;
        this.us = us;
        this.ms = ms;
        this.auth = auth;
        this.rss = rss;
        this.uss = uss;
        this.bcs = bcs;
        this.dci = dci;
    }

    // Todo - 클래스 버전 변경 이유 찾기.

    @Override
    public void run(String... args) {
        log.info(dci.getFilePath().toString());
        log.info(dci.getRepoType());
    }
}
