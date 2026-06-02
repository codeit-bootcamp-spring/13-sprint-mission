package com.sprint.mission.discodeit.service.file;


import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FileMessageService implements MessageService {


    //private final Path messagePath =Path.of("data/messages.csv"); -> FileMessageRepository
    private final MessageRepository messageRepository=new FileMessageRepository();

    @Override
    public Message createOne(UUID channelId, String content, Long createdAt) throws IOException {

        Message message=new Message(channelId, content, createdAt);

//        저장로직 -> repository
//        Path parent=messagePath.getParent(); // 경로 실제로 존재하는지 확인하기 위해 부모 경로 확인
//
//        if(parent!=null){ // null 체크 먼저 진행하자
//            Files.createDirectories(parent);
//        }
//
//        try (BufferedWriter writer=Files.newBufferedWriter(messagePath, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE)){
//            writer.write(message.getId()+","+message.getChannelId()+","+message.getContent()+","+message.getCreatedAt()+","+message.getUpdatedAt());
//            writer.newLine();
//        }
        return messageRepository.createOne(message);

//        return message;
    }

    @Override
    public Optional<Message> readOne(UUID id) throws IOException {

//        try (BufferedReader reader=Files.newBufferedReader(messagePath, StandardCharsets.UTF_8)){
//
//            String line;
//            while ((line=reader.readLine())!=null){
//                if (line.isBlank()) continue;
//                Message message=parseCsvRow(line);
//                if (message.getId().equals(id)){
//                    return Optional.of(message);
//                }
//            }
//        }
//        return Optional.empty();
        return messageRepository.readOne(id);
    }

//    private Message parseCsvRow(String line) throws IOException{
//
//        String[] cols=line.split(",", -1);
//        if (cols.length<4) {
//            throw new IOException("CSV 칼럼 수가 부족합니다! (4개 필요, 실제 "+cols.length+"개)");
//        }
//        UUID id=UUID.fromString(cols[0]);
//        UUID channelId=UUID.fromString(cols[1]);
//        String content=cols[2];
//        Long createdAt=Long.parseLong(cols[3]);
//        return new Message(id, channelId, content, createdAt);
//    }

    @Override
    public List<Message> readAll() throws IOException {

//        List<Message> allResult=new ArrayList<>();
//        try (BufferedReader reader=Files.newBufferedReader(messagePath, StandardCharsets.UTF_8)){
//
//
//            String line;
//            while ((line=reader.readLine())!=null){
//                if (line.isBlank()) continue;
//                allResult.add(parseCsvRow(line));
//            }
//        }
//        return allResult;
        return messageRepository.readAll();
    }

    @Override
    public Message editOne(UUID id, UUID newChannelId, String newContent, Long updatedAt) throws IOException {


//        List<Message> update=new ArrayList<>();
//        try (BufferedReader reader=Files.newBufferedReader(messagePath, StandardCharsets.UTF_8)){
//
//
//            String line;
//            while ((line=reader.readLine())!=null){
//                if (line.isBlank()) continue;
//                Message message=parseCsvRow(line);
//                if (message.getId().equals(id)){
//                    message.updateMessage(newChannelId, newContent, updatedAt);
//                }
//                update.add(message);
//            }
//        }
//
//        try (BufferedWriter writer=Files.newBufferedWriter(messagePath, StandardCharsets.UTF_8)){
//
//            for (Message message : update) {
//                writer.write(message.getId()+","+message.getChannelId()+","+message.getContent()+","+message.getCreatedAt()+","+message.getUpdatedAt());
//                writer.newLine();
//            }
//        }
//        return update.stream()
//                .filter(message -> message.getId().equals(id))
//                .findFirst()
//                .orElseThrow();
        Message message=messageRepository.readOne(id)
                .orElseThrow();
        message.updateMessage(newChannelId, newContent, updatedAt);
        messageRepository.createOne(message);

        return message;


    }

    @Override
    public void deleteOne(UUID id) throws IOException {
//        List<Message> erase=new ArrayList<>();
//        try (BufferedReader reader=Files.newBufferedReader(messagePath, StandardCharsets.UTF_8)){
//
//            String line;
//            while ((line=reader.readLine())!=null) {
//                if (line.isBlank()) continue;
//                Message message=parseCsvRow(line);
//                if (!message.getId().equals(id)){
//                    erase.add(message);
//                }
//            }
//        }
//
//        try (BufferedWriter writer=Files.newBufferedWriter(messagePath, StandardCharsets.UTF_8)){
//            for (Message message : erase) {
//                writer.write(message.getId()+","+message.getChannelId()+","+message.getContent()+","+message.getUpdatedAt());
//                writer.newLine();
//            }
//        }
        messageRepository.deleteOne(id);

    }

}
