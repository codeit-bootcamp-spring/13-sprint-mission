package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileBinaryContentRepository implements BinaryContentRepository {

    private final List<BinaryContent> binaryContents = new ArrayList<>();
    private final Path binaryContentPath;


    public FileBinaryContentRepository(Path binaryContentPath) {
        this.binaryContentPath = binaryContentPath;
        loadFromFile();
    }

    public FileBinaryContentRepository() {
        this(Paths.get("data/binaryContents.ser"));
    }

    @Override
    public List<BinaryContent> findAllByMessageId(UUID id) {
        return binaryContents.stream()
                .filter(content -> content.getMessageId().equals(id))
                .toList();
    }

    @Override
    public BinaryContent findByUserId(UUID id) {
        return binaryContents.stream()
                .filter(content -> content.getUserId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void create(BinaryContent binaryContent) {
        binaryContents.add(binaryContent);
        saveToFile();
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        return binaryContents.stream()
                .filter(content -> ids.contains(content.getId()))
                .toList();
    }

    @Override
    public BinaryContent find(UUID id) {
        return binaryContents.stream()
                .filter(content -> content.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void delete(UUID id) {
        binaryContents.removeIf(binaryContent ->
                Objects.equals(binaryContent.getId(), id)
        );
        saveToFile();
    }

    private void saveToFile() {
        try {
            Path parent = binaryContentPath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(binaryContentPath)))) {

                oos.writeObject(binaryContents);
            }

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        if (!Files.exists(binaryContentPath)) {
            return;
        }

        try(ObjectInputStream ois = new ObjectInputStream
                (new BufferedInputStream(Files.newInputStream(binaryContentPath)))) {

            List<BinaryContent> loadedBinary = (List<BinaryContent>) ois.readObject();
            binaryContents.clear();
            binaryContents.addAll(loadedBinary);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("파일을 불러오기 중에 오류가 발생했습니다.", e);
        }
    }


}




