package com.sprint.mission.discodeit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//파일 저장소 관련 설정 정보를 관리하는 클래스
@Getter //모든 필드에 대한 Getter 메서드 자동생성
@Setter //모든 필드에 대한 Setter 메서드 자동생성
@Component //Spring Bean으로 등록
@ConfigurationProperties(prefix = "discodeit.storage")
public class StorageProperties {
    private  String rootPath; //파일이 저장될 최상위 디렉토리 경로
    private  String extension; //저장 파일의 확장자
}
