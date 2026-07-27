package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.config.DiscodeitConfig;

import com.sprint.mission.discodeit.service.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class DiscodeitInit implements CommandLineRunner {
    DiscodeitConfig dci;


    public DiscodeitInit(
            DiscodeitConfig dci
            ) {
        this.dci = dci;
    }

    // Todo - 클래스 버전 변경 이유 찾기.

    @Override
    public void run(String... args) {
    }
}
