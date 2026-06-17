package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.EntityRoot;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class FileRepositoryRoot<T>{

    //entity 저장 객체 및 저장 경로
    protected final List<T> storage = new ArrayList<>();
    protected final Path binPath;

    //ctor
    public FileRepositoryRoot(Path binPath) {
        this.binPath = binPath;

        // 파일 저장 위치에 파일이 존재한다면 로드하도록.
        if (Files.exists(binPath))
            loadFromBinary();
    }

    //method
    //직렬화 메서드
    protected void saveToBinary() {
        Path parent = binPath.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new RuntimeException("폴더 생성에 실패했습니다.");
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(binPath)))) {
            oos.writeObject(new ArrayList<>(storage));
        } catch (IOException e) {
            throw new RuntimeException("직렬화에 실패했습니다.");
        }
    }

    //역직렬화 메서드
    protected void loadFromBinary() {
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(binPath)))) {
            List<T> storageTemp = (List<T>) ois.readObject();
            // 일단 storage 초기화
            storage.clear();
            // storage에 역직렬화한 List<T> 넣기
            storage.addAll(storageTemp);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("역직렬화에 실패했습니다.");
        } catch (IOException e) {
            throw new RuntimeException("역직렬화에 실패했습니다.");
        }
    }
}
