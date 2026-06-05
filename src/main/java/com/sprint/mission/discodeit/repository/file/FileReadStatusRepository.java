package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class FileReadStatusRepository extends FileBaseRepository implements ReadStatusRepository {
    private final Path DIRECTORY = Paths.get(System.getProperty("user.dir"),"data","readstatus");

    @Override
    public void save(ReadStatus rs) {
        try {
            write(DIRECTORY.resolve(rs.getId()+ ".ser"), rs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ReadStatus> find(Predicate<ReadStatus> fn) {
        try (
                Stream<Path> paths = Files.list(DIRECTORY)
        ){
            return paths.map(c -> {
                        try {
                            return (ReadStatus) read(DIRECTORY.resolve(c));
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }})
                    .filter(fn)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ReadStatus findByID(UUID id) {
        return find(rs -> rs.getId().equals(id)).get(0);
    }

    @Override
    public void delete(UUID id) {
        try {
            Files.delete(DIRECTORY.resolve(id.toString() + ".ser"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
