package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FileBinaryContentRepository implements BinaryContentRepository {

    private final String filePath = "binary_content.dat";

    @SuppressWarnings("unchecked")
    private List<BinaryContent> loadAll() {
        File file = new File(filePath);
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<BinaryContent>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void saveAll(List<BinaryContent> list) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void create(BinaryContent binaryContent) {
        List<BinaryContent> list = loadAll();
        list.add(binaryContent);
        saveAll(list);
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        return loadAll().stream().filter(bc -> bc.getId().equals(id)).findFirst();
    }

    @Override
    public List<BinaryContent> findAll() {
        return loadAll();
    }

    @Override
    public void delete(UUID id) {
        List<BinaryContent> list = loadAll();
        list.removeIf(bc -> bc.getId().equals(id));
        saveAll(list);
    }
}
