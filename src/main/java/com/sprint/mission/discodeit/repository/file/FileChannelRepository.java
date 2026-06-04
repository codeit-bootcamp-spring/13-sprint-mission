package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
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

public class FileChannelRepository implements ChannelRepository {

    private static final String channelFile = "channels.csv"; // 파일명만 static하게 상수 표현

    private Channel parseCsvRow(String line) throws IOException {
        String[] cols=line.split(",", -1);
        if (cols.length<4){
            throw new IOException("CSV 칼럼 수가 부족합니다! (4개 필요, 실제 "+cols.length+"개)");
        }
        UUID id=UUID.fromString(cols[0]);
        ChannelType type= ChannelType.valueOf(cols[1]);
        String name=cols[2];
        Long createdAt=Long.parseLong(cols[3]);
        return new Channel(id, type, name, createdAt);
    }

    @Override
    public Channel saveChannel(Channel channel) throws IOException {
        Path channelPath=Path.of("data", channelFile); // 경로 포함한 파일명은 파일을 읽는 부분에서 처리
        Path parent=channelPath.getParent(); // 경로 실제로 존재하는지 확인하기 위해 부모 경로 확인
        if(parent!=null){ // null 체크 먼저 진행하자
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer= Files.newBufferedWriter(channelPath, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE)){
            writer.write(channel.getId()+","+channel.getType()+","+channel.getName()+","+channel.getCreatedAt()+","+channel.getUpdatedAt());
            writer.newLine();
        }
        return channel;
    }

    @Override
    public Optional<Channel> fineChannel(UUID id) throws IOException {
        Path channelPath=Path.of("data", channelFile);
        try (BufferedReader reader=Files.newBufferedReader(channelPath, StandardCharsets.UTF_8)){
            String line;
            while ((line=reader.readLine())!=null){
                if (line.isBlank()) continue;
                Channel channel=parseCsvRow(line);
                if (channel.getId().equals(id)){
                    return Optional.of(channel);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Channel> findChannels() throws IOException {
        List<Channel> allResult =new ArrayList<>();
        Path channelPath=Path.of("data", channelFile);
        try(BufferedReader reader=Files.newBufferedReader(channelPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line=reader.readLine())!=null) {
                if (line.isBlank()) continue;
                allResult.add(parseCsvRow(line));
            }
        }
        return allResult;
    }

    @Override
    public void deleteChannel(UUID id) throws IOException {
        List<Channel> erase=new ArrayList<>();
        Path channelPath=Path.of("data", channelFile);
        try (BufferedReader reader=Files.newBufferedReader(channelPath, StandardCharsets.UTF_8)){
            String line;
            while ((line=reader.readLine())!=null) {
                if (line.isBlank()) continue;
                Channel channel=parseCsvRow(line);
                if (!channel.getId().equals(id)) {
                    erase.add(channel);

                }
            }

        }

        try (BufferedWriter writer=Files.newBufferedWriter(channelPath, StandardCharsets.UTF_8)){
            for (Channel channel : erase) {
                writer.write(channel.getId()+","+channel.getType()+","+channel.getName()+","+channel.getUpdatedAt());
                writer.newLine();
            }
        }
    }
}
