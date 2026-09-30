package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.config.storage.LocalConfig;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "discodeit.storage", name = "type", havingValue = "local", matchIfMissing = true)
@Slf4j
public class LocalBinaryContentStorage implements BinaryContentStorage {

    private final LocalConfig localConfig;

    @Override
    public UUID put(UUID id, byte[] content) {
        writeFile(id.toString(), content);
        return id;
    }
    @Override
    public InputStream get(UUID id) throws IOException {
        return input(id.toString());
    }

    @Override
    public ResponseEntity<Resource> download(BinaryContentDto binaryContentDto) {
        try{
            return ResponseEntity.status(HttpStatus.OK)
                    .body(
                            new InputStreamResource(input(binaryContentDto.fileName()))
                    );
        } catch (IOException e){
            log.error("LocalBinaryContentStorage - 파일 리소스 응답 생성 에러 - {}",binaryContentDto.fileName());
            throw new RuntimeException(e);
        }

    }

    @Override
    public void delete(UUID id){
        delete(id.toString());
    }


    /*
    데이터 입출력 매서드
     */
    private void writeFile(String path, byte[] contents){
        try (BufferedOutputStream stream = output(path)) {
            stream.write(contents);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void delete(String path) {
        try {
            Files.delete(localConfig.resolvePath(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    Stream 반환 매서드
     */
    private BufferedOutputStream output(String path) throws IOException {
        return new BufferedOutputStream(Files.newOutputStream(localConfig.resolvePath(path)));
    }

    private BufferedInputStream input(String path) throws IOException {
        return new BufferedInputStream(Files.newInputStream(localConfig.resolvePath(path)));
    }

}
