package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileBinaryContentRepository implements BinaryContentRepository {

    private final Path filePath;

    public FileBinaryContentRepository(
            @Value("${discodeit.repository.file-directory:.discodeit}") String fileDirectory
    ) {
        this.filePath = Path.of(fileDirectory).resolve("binary.ser");
        if (!Files.exists(filePath.getParent())){
            try{
                Files.createDirectories(filePath.getParent());
            }catch (IOException e){
                throw new RuntimeException("디렉토리 생성 실패");
            }
        }
    }

    //data 저장
    private void saveToFile(Map<UUID, BinaryContent> data){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))){
            oos.writeObject(data);
        }catch (IOException e){
            throw new RuntimeException("파일 저장 실패!");
        }
    }

    //파일 불러오기
    private Map<UUID, BinaryContent> loadFromFile(){
        if (!Files.exists(filePath)){
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toFile()))){
            return (Map<UUID, BinaryContent>) ois.readObject();
        }catch (IOException | ClassNotFoundException e){
            throw new RuntimeException("파일 불러오기 실패");
        }
    }

    @Override
    public void save(BinaryContent binaryContent) {
        Map<UUID, BinaryContent> data = loadFromFile();
        data.put(binaryContent.getId(), binaryContent);
        saveToFile(data);
    }

    @Override
    public Optional<BinaryContent> find(UUID id) {
        Map<UUID, BinaryContent> data = loadFromFile();
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public void delete(UUID id) {
        Map<UUID, BinaryContent> data = loadFromFile();
        data.remove(id);
        saveToFile(data);
    }
}
