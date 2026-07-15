package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;

public interface BinaryContentStorage {

    // 파일을 디스크에 저장하고 저장 위치의 키(UUID)를 반환
    UUID put(UUID id, byte[] bytes);

    // 저장된 파일을 InputStream으로 읽어서 반환
    InputStream get(UUID id);

    // HTTP 다운로드 응답을 생성해서 반환
    ResponseEntity<Resource> download(BinaryContentDto binaryContentDto);
}