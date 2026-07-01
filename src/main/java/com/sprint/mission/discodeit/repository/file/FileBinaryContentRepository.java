package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@ConditionalOnProperty(
        prefix = "discodeit.repository",
        name = "type",
        havingValue = "file"
)
public class FileBinaryContentRepository implements BinaryContentRepository {

    private final Path path;

    public FileBinaryContentRepository(@Value("${file.path.binaryContent}") String path) {
        this.path = Paths.get(path);

        try {
            Files.createDirectories(this.path.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveFile(List<BinaryContent> binaryContents) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            oos.writeObject(new ArrayList<>(binaryContents));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<BinaryContent> loadFile() {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            return (List<BinaryContent>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void save(BinaryContent binaryContent) {
        List<BinaryContent> binaryContents = loadFile();

        boolean isUpdated = false;
        for (int i = 0; i < binaryContents.size(); i++) {
            if (binaryContents.get(i).getId().equals(binaryContent.getId())) {
                binaryContents.set(i, binaryContent);
                isUpdated = true;
                break;
            }
        }

        if(!isUpdated) {
            binaryContents.add(binaryContent);
        }
        saveFile(binaryContents);
    }

    @Override
    public BinaryContent findById(UUID id) {
        List<BinaryContent> binaryContents = loadFile();

        for (BinaryContent binaryContent : binaryContents) {
            if(binaryContent.getId().equals(id)) {
                return binaryContent;
            }
        }
        return null;
    }

    @Override
    public List<BinaryContent> findAll() {
        return loadFile();
    }

    @Override
    public void delete(UUID id) {
        List<BinaryContent> binaryContents = loadFile();

        if(binaryContents.removeIf(binaryContent -> binaryContent.getId().equals(id))) {
            saveFile(binaryContents);
        }
    }
}
