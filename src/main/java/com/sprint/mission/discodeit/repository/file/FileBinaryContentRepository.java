package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;

@Repository
public class FileBinaryContentRepository  implements BinaryContentRepository {

    private final File binaryContentFile;
    private final Map<UUID, BinaryContent> binaryContentsRepo;
    private Map<UUID, BinaryContent> loadBinaryContentsRepo()  {
        if (!binaryContentFile.exists()) {
            return new HashMap<>();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(binaryContentFile))) {
            return (Map<UUID, BinaryContent>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<>();
        }
    }

    public FileBinaryContentRepository() {
        this.binaryContentFile = new File("data/repository-binary-content.json");
        this.binaryContentsRepo = loadBinaryContentsRepo();
    }

    private void saveBinaryContentRepo() {
        File parent = binaryContentFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(binaryContentFile))) {
            out.writeObject(binaryContentsRepo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(BinaryContent binaryContent) {
        binaryContentsRepo.put(binaryContent.getId(), binaryContent);
        saveBinaryContentRepo();
    }

    @Override
    public BinaryContent findById(UUID id) {
        return binaryContentsRepo.get(id);
    }

    @Override
    public Collection<BinaryContent> findAllByIdIn(Collection<UUID> ids) {
        List<BinaryContent> result = new ArrayList<>();

        for (UUID id : ids) {
            BinaryContent binaryContent = binaryContentsRepo.get(id);
            if (binaryContent != null) {
                result.add(binaryContent);
            }
        }return result;
    }

    @Override
    public void delete(UUID id) {
        binaryContentsRepo.remove(id);
        saveBinaryContentRepo();

    }
}
