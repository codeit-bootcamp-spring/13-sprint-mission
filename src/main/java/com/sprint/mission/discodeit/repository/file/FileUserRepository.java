package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;



public class FileUserRepository implements UserRepository {

    private final Path userPath =Path.of("data/users.csv");

    @Override
    public User createOne(User user) throws IOException {

        Path parent=userPath.getParent(); // 경로 실제로 존재하는지 확인하기 위해 부모 경로 확인

        if(parent!=null){ // null 체크 먼저 진행하자
            Files.createDirectories(parent);
        }

        try(BufferedWriter writer=Files.newBufferedWriter(userPath, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE)){
            writer.write(user.getId()+","+user.getUsername()+","+user.getEmail()+","+user.getCreatedAt()+","+user.getUpdatedAt()); // 공백 조심
            writer.newLine();
        }
        return user;
    }

    @Override
    public Optional<User> readOne(UUID id) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(userPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                User user = parseCsvRow(line);
                if (user.getId().equals(id)) {
                    return Optional.of(user);
                }

            }
        }
        return Optional.empty();
    }

    public User parseCsvRow(String line) throws IOException{
        String[] cols=line.split(",", -1);
        if (cols.length<4){
            throw new IOException("CSV 칼럼 수가 부족합니다! (4개 필요, 실제 "+cols.length+"개)");
        }
        UUID id=UUID.fromString(cols[0]);
        String username=cols[1];
        String email=cols[2];
        Long createdAt=Long.parseLong(cols[3]);

        return new User(id, username, email, createdAt);


    }

    @Override
    public List<User> readAll() throws IOException {
        List<User> allResult=new ArrayList<>();
        try (BufferedReader reader=Files.newBufferedReader(userPath, StandardCharsets.UTF_8)) {


            String line;
            while ((line=reader.readLine())!=null){
                if (line.isBlank()) continue;
                allResult.add(parseCsvRow(line));
            }
        }
        return allResult;
    }


    @Override
    public void deleteOne(UUID id) throws IOException {
        List<User> erase=new ArrayList<>();
        try (BufferedReader reader=Files.newBufferedReader(userPath, StandardCharsets.UTF_8)) {


            String line;
            while ((line=reader.readLine())!=null){
                if (line.isBlank()) continue;
                User user = parseCsvRow(line);
                if (!user.getId().equals(id)) {
                    erase.add(user); // 아이디가 일치하는 것은 빼고 일치하지 않는 것만 추가하자
                }

            }
        }

        try(BufferedWriter writer=Files.newBufferedWriter(userPath, StandardCharsets.UTF_8)){
            for (User user : erase) {

                writer.write(user.getId()+","+user.getUsername()+","+user.getEmail()+","+user.getUpdatedAt());
                writer.newLine();
            }

        }

    }


}
