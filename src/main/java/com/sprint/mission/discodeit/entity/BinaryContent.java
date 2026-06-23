package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
//이미지,파일 등 바이너리 데이터를 표현하는 도메인 모델(사용자의 프로필 이미지, 메시지에 첨부된 파일을 저장하기 위해 활용됨)
public class BinaryContent implements Serializable {
    private static final long SERIAL_VERSION_UID = 1L; //aerializable 클래스의 버저 식별자
    private final UUID id; //바이너리 파일의 고유 식별자(UUID). 파일 생성 시 자동으로 생성되며,시스템 내에서 파일을 구분하는 기본 키 역할을 수행함.
    private String fileName; //파일 이름
    private String contentType; //파일 MIME 타입
    private byte[] bytes; //실제 파일 데이터
    private Instant updatedAt; //파일 정보가 마지막으로 수정된 시각
    private Instant createdAt; //파일이 최초 생성된 시각

    public BinaryContent(String fileName, String contentType, byte[] bytes) {
        this.id = UUID.randomUUID(); //고유 식별자 생성
        this.fileName = fileName; //파일명 저장
        this.contentType = contentType; //파일 타입 저장
        this.createdAt = Instant.now(); //객체 생성 시각 저장
        this.bytes = bytes; //파일 데이터 저장
    }

}
