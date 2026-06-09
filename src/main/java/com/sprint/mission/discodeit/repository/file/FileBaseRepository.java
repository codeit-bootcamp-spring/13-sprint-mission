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

    static void check(Path path) throws IOException {
        if(!Files.exists(path.getParent())){
            Files.createDirectories(path.getParent());
        }
    }

    static <T> T read(Path path) throws IOException {
        try (
                BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(path));
                ObjectInputStream ois = new ObjectInputStream(bis)
        ){
            return (T) ois.readObject();
        } catch(ClassNotFoundException e) { // 클래스 데이터 확인 불가
            throw new IOException(e);
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


    // Todo - pre -> return 2중변환 로직 변경. 변환수 적게 만듦
    static <T> List<T> rawFind(Predicate<T> fn, Path path) throws RuntimeException {
        try {
            if(!Files.exists(path)){
                Files.createDirectories(path);
            }
            try (Stream<Path> paths = Files.list(path)) {
                List<T> pre = paths.map(c -> {
                            try {
                                return (T) read(path.resolve(c));
                            } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                            }
                        })
                        .filter(Objects::nonNull).toList();
                if (pre.isEmpty()) {
                    return pre;
                } else{
                    return pre.stream()
                            .filter(fn)
                            .toList();
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Path filePath(Path path,String dir,String fileName){
        return path.resolve(dir + fileName + ".ser");
    }
}
