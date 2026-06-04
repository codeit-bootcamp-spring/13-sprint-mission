package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BaseEntity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBaseRepository {

    static void check(Path path) throws IOException {
        if(!Files.exists(path.getParent())){
            Files.createDirectories(path.getParent());
        }
    }

    static <T extends BaseEntity> T read(Path path) throws IOException, ClassNotFoundException {
        check(path);
        try (
                BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(path));
                ObjectInputStream ois = new ObjectInputStream(bis)
        ){
            return (T) ois.readObject();
        }
    }

    static <T> void write(Path path,T entity) throws IOException {
        check(path);
        try (
                ObjectOutputStream oos = new ObjectOutputStream(
                        new BufferedOutputStream(Files.newOutputStream(path))
                )
        ){
            oos.writeObject(entity);
        }
    }
}
