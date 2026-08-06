package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import jakarta.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
@NoArgsConstructor
@ConditionalOnProperty(prefix = "discodeit.storage", name = "type", havingValue = "local")
@Slf4j
public class LocalBinaryContentStorage implements BinaryContentStorage {

    @Value(value = "${discodeit.storage.local.root-path}")
    private Path root;

    @PostConstruct
    void init(){

        log.debug("local storage check - {}", Path.of(root.toString()));

        if (Files.notExists(root)){
            try {
                Files.createDirectories(root);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public UUID put(UUID id, byte[] content) {
        try (
                OutputStream out = Files.newOutputStream(resolvePath(id));
                BufferedOutputStream but = new BufferedOutputStream(out)
                ){

            but.write(content);

            log.debug("file write on path - {}", resolvePath(id));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return id;
    }
    @Override
    public InputStream get(UUID id) throws IOException{
        InputStream in = Files.newInputStream(resolvePath(id));
        return new BufferedInputStream(in);
    }

    @Override
    public ResponseEntity<Resource> download(BinaryContentDto binaryContentDto) {
        try{
            InputStream in = get(binaryContentDto.id());
            return ResponseEntity.status(HttpStatus.OK).body(
                    new InputStreamResource(in)
            );
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private Path resolvePath(UUID id){
        return root.resolve(id.toString());
    }

}
