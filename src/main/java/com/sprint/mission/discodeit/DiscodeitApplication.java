package com.sprint.mission.discodeit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class DiscodeitApplication {

  public static void main(String[] args) {
    SpringApplication.run(DiscodeitApplication.class, args);
  }

//  @Bean
//  @Profile("dev")
//  public ApplicationRunner initDummyFiles(BinaryContentRepository fileRepository) {
//    return args -> {
//      String basePath = "/Users/apple/Desktop/codeit-13th-develop/13-sprint-mission/dummy/";
//
//      String[] dummyIds = {
//          "00000000-0000-0000-0000-000000000001",
//          "00000000-0000-0000-0000-000000000002",
//          "00000000-0000-0000-0000-000000000003",
//          "00000000-0000-0000-0000-000000000004",
//          "00000000-0000-0000-0000-000000000005"
//      };
//
//      for (int i = 0; i < 5; i++) {
//        UUID fixedId = UUID.fromString(dummyIds[i]);
//        String filename = "dummy" + (i + 1) + ".png";
//
//        BinaryContent dummyContent = BinaryContent.builder()
//            .id(fixedId)
//            .createdAt(Instant.now())
//            .messageId(null)
//            .fileName(filename)
//            .fileUrl(basePath + filename)
//            .size(1024L)
//            .build();
//
//        fileRepository.save(dummyContent);
//        System.out.println(">>> [더미 로드 완료] 파일명: " + filename + " | ID: " + fixedId);
//      }
//    };
//  }
}



