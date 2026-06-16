package com.sprint.mission.discodeit.repository.file;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;


// Todo - IOException retry logic and throws.
public class FileBaseRepository {

    static <T> void write(Path path,T entity) {
        try{
            if(!Files.exists(path.getParent())){
                Files.createDirectories(path.getParent());
            }

            OutputStream fis = Files.newOutputStream(path);
            BufferedOutputStream bis = new BufferedOutputStream(fis);
            ObjectOutputStream oos = new ObjectOutputStream(bis);

            oos.writeObject(entity);

            oos.close();
            bis.close();
            fis.close();
        } catch(IOException e){
            throw new RuntimeException(e.getMessage(), e.getCause());
        }

    }

    static void delete(Path path) {
        try {
            Files.delete(path);
        } catch(IOException e){
            throw new RuntimeException(e.getMessage(), e.getCause());
        }

    }

    static <T> List<T> read(Predicate<T> fn, Path path) {
        try {
            if(!Files.exists(path)){
                Files.createDirectories(path);
            }

            Stream<Path> ps =  Files.list(path);

            return ps.map(c -> {
                try(
                        BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(path));
                        ObjectInputStream ois = new ObjectInputStream(bis)
                ){
                    return (T) ois.readObject();
                } catch (ClassNotFoundException | IOException e) {
                    return null;
                }
            }).filter(Objects::nonNull).toList();
        } catch(IOException e){
            throw new RuntimeException(e.getMessage(), e.getCause());
        }

    }
}
