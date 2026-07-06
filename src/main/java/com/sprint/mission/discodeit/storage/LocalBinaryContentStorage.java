package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import jakarta.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Controller
@NoArgsConstructor
@ConditionalOnProperty(prefix = "discodeit.storage.type", value = "local")
public class LocalBinaryContentStorage implements BinaryContentStorage {
    @Value(value = "${discodeit.storage.local.root-path}")
    private Path root;

    @PostConstruct
    void init(){
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return id;
    }
    @Override
    public InputStream get(UUID id) {
        try (
                InputStream in = Files.newInputStream(resolvePath(id));
                BufferedInputStream bin= new BufferedInputStream(in)
                ){
            return bin;
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    @Override
    public ResponseEntity<Resource> download(BinaryContentDto binaryContentDto) {
        return ResponseEntity.status(HttpStatus.OK).body(
                new InputStreamResource(get(binaryContentDto.id()))
        );
    }

    private Path resolvePath(UUID id){
        return root.resolve(id.toString());
    }

}
