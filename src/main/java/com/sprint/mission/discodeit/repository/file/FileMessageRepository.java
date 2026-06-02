package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

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

public class FileMessageRepository implements MessageRepository {

    private final Path messagePath =Path.of("data/messages.csv");


    @Override
    public Message createOne(Message message) throws IOException {
        Path parent=messagePath.getParent(); // 경로 실제로 존재하는지 확인하기 위해 부모 경로 확인

        if(parent!=null){ // null 체크 먼저 진행하자
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer= Files.newBufferedWriter(messagePath, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE)){
            writer.write(message.getId()+","+message.getChannelId()+","+message.getContent()+","+message.getCreatedAt()+","+message.getUpdatedAt());
            writer.newLine();
        }
        return message;
    }

    @Override
    public Optional<Message> readOne(UUID id) throws IOException {
        try (BufferedReader reader=Files.newBufferedReader(messagePath, StandardCharsets.UTF_8)){

            String line;
            while ((line=reader.readLine())!=null){
                if (line.isBlank()) continue;
                Message message=parseCsvRow(line);
                if (message.getId().equals(id)){
                    return Optional.of(message);
                }
            }
        }
        return Optional.empty();
    }

    private Message parseCsvRow(String line) throws IOException{

        String[] cols=line.split(",", -1);
        if (cols.length<4) {
            throw new IOException("CSV 칼럼 수가 부족합니다! (4개 필요, 실제 "+cols.length+"개)");
        }
        UUID id=UUID.fromString(cols[0]);
        UUID channelId=UUID.fromString(cols[1]);
        String content=cols[2];
        Long createdAt=Long.parseLong(cols[3]);
        return new Message(id, channelId, content, createdAt);
    }

    @Override
    public List<Message> readAll() throws IOException {
        List<Message> allResult=new ArrayList<>();
        try (BufferedReader reader=Files.newBufferedReader(messagePath, StandardCharsets.UTF_8)){


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
        List<Message> erase=new ArrayList<>();
        try (BufferedReader reader=Files.newBufferedReader(messagePath, StandardCharsets.UTF_8)){

            String line;
            while ((line=reader.readLine())!=null) {
                if (line.isBlank()) continue;
                Message message=parseCsvRow(line);
                if (!message.getId().equals(id)){
                    erase.add(message);
                }
            }
        }

        try (BufferedWriter writer=Files.newBufferedWriter(messagePath, StandardCharsets.UTF_8)){
            for (Message message : erase) {
                writer.write(message.getId()+","+message.getChannelId()+","+message.getContent()+","+message.getUpdatedAt());
                writer.newLine();
            }
        }
    }
}
