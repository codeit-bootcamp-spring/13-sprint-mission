package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
public class LocalBinaryContentStorage implements BinaryContentStorage {


  private final Path root;
  //생성자에서 @Value로 application.yml의 설정값을 주입받음.
  //


  public LocalBinaryContentStorage(
      @Value("${discodeit.storage.local.root-path}") String rootPath) {
    //경로로 서버 로컬 디스크에서 파일을 모아둘 폴더의 이름을 가져옴.
    this.root = Paths.get(rootPath);
    //Paths.get(".storage")  → ".storage" 문자열을 Path 객체로 변환
  }

  @PostConstruct//빈 생성 직후 자동 호출(실행)
  public void init() {
    //File 클래스 파일을 다루는 자바 유틸 클래스.
    try {
      Files.createDirectories(root);
      //createDirectories-> 디렉토리 없으면 생성 있으면 동작 안함
    } catch (IOException e) {
      throw new RuntimeException("루트 디렉토리 생성실패", e);
    }//-> 서버 로컬 디스크 용량 부족, 권한 없을때 예외.
  }


  @Override
  public UUID put(UUID id, byte[] bytes) {
    // UUID를 파일 이름으로 서버 로컬 디스크에 저장
    Path filePath = resolvePath(id);
    try {
      Files.write(filePath, bytes);
      //지정한 경로에 byte데이터를 파일로 저장하는 메서드.
    } catch (IOException e) {
      throw new RuntimeException("파일 저장 실패 - id: " + id, e);
    }
    return id;//서버 로컬에 저장한 파일을 찾을 때 써야하니까 UUID 반환
  }

  @Override
  public InputStream get(UUID id) {
    // UUID 이름의 파일을 InputStream으로 읽어옴
    Path filePath = resolvePath(id);
    try {
      return Files.newInputStream(filePath);
      //지정한 경로의 파일을 읽을 수 있는 InputStream(통로)을 반환하는 메서드
    } catch (IOException e) {
      throw new RuntimeException("차일 읽기 실패 - id:" + id, e);
    }
  }

  @Override
  public ResponseEntity<?> download(BinaryContentDto binaryContentDto) {
    // InputStream으로 파일 읽어서 HTTP 응답으로 전송
    InputStream inputStream = get(binaryContentDto.id());
    InputStreamResource resource = new InputStreamResource(inputStream);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,//.header: 브라우저에게 응답 처리 방법을 알려줌.
            ContentDisposition.attachment()
                //다운로드 파일로 처리 (attachment = 첨부파일)
                .filename(binaryContentDto.fileName())
                // 다운로드될 파일 이름 (예: photo.jpg)
                .build().toString())
        //"attachment; filename=photo.jpg" 문자열로 변환
        .contentType(MediaType.parseMediaType(binaryContentDto.contentType()))
        //contentType: 브라우저한테 파일 형식을 알려줌
        //MediaType.parseMediaType 문자열을 MediaType 객체로 변환
        .contentLength(binaryContentDto.size())
        //contentLength 파일 크기를 알려줌(long타입 그대로)
        .body(resource);
    //응답 url body에 읽어온 파일을 담아서 응답
  }

  //--헹퍼 메서드--
  private Path resolvePath(UUID id) {
    //UUID를 파일 저장 경로로 변환하는 메서드
    //put(),get() 둘다 사용 그래서 메서드로 만듬.
    return root.resolve(id.toString());
  }

}
