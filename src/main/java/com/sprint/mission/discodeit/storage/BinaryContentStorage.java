package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public interface BinaryContentStorage {

  UUID put(UUID id, byte[] bytes);
  //byte[] bytes: 실제 2진수로 이루어진 파일.
  //UUID id를 해당 파일을 서버의 로컬에 저장할때 이름으로 사용(중복 파일 이름 충돌 방지)
  //사용자 로컬, DB가 아닌 서버의 로컬 저장소임.

  InputStream get(UUID id);
  //InputStream: 파일 데이터를 읽어오는 stream 타입
  //-> 파일 전체를 한번에 읽어오지 않고 조금씩 읽어와 메모리에 부담을 줄인다.
  //로컬에 UUID id를 이름으로 갖는 파일을 읽어오는 통로.
  //byte[] get(UUID id)은 한번에 파일을 읽어온다.
  // 이미지, 동영상등 큰파일을 한번에 읽어오면 메모리에 무리가 올 수도 있다.
  //아는 서버에 부담이 될수 있어서  InputStream로 안전하게 읽어온다.
  //서버의 로컬에서 파일을 읽어오기만함.

  ResponseEntity<?> download(BinaryContentDto binaryContentDto);
  //?: 와일드키드이며 무슨 타입이든 <>제너럴안에 들어 올 수 있다.
  //InputStream get(UUID id)로 읽어온 파일을 사용자에게 HTTP응답으로 전솔
  //사용자가 파일을 다운 받을 수 있음.
}
