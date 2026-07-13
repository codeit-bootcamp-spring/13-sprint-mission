package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public interface BinaryContentStorage { // 바이너리 데이터의 저장/로드 담당

  UUID put(UUID id, byte[] bytes); // UUID 키 정보 바탕으로 byte[] 데이터 저장

  InputStream get(UUID id); // 키 정보를 바탕으로 byte[] 데이터를 읽어 InputStream 타입으로 반환

  ResponseEntity<?> download(BinaryContentDto binaryContentDto);
  // BinaryContentDto 정보를 바탕으로 파일 다운로드할 수 있는 응답 반환
}
