package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository repository;
    private final ReadStatusRepository readStatusRepository;


    @Override
    public ChannelResponse createPublicChannel(ChannelRequest.CreatePublicChannel publicChannel) {
        if (publicChannel == null) {
            throw new IllegalArgumentException("공개 채널 생성 요청은 필수입니다.");
        }
        Channel channel = new Channel(
                publicChannel.name(),
                publicChannel.description(),
                ChannelType.PUBLIC
        );
        repository.create(channel);

        return ChannelResponse.from(
                channel,
                null,
                List.of()
        );

    }

    @Override
    public ChannelResponse createPrivateChannel(ChannelRequest.CreatePrivateChannel privateChannel) {
        if (privateChannel == null) {
            throw new IllegalArgumentException("비공개 채널 생성 요청은 필수입니다.");
        }

        Channel channel = new Channel(
                null,
                null,
                ChannelType.PRIVATE
        );

        repository.create(channel);

        for (UUID participantId : privateChannel.participantIds()) {
            ReadStatus readStatus = new ReadStatus(
                    participantId,
                    channel.getId()
            );

            readStatusRepository.create(readStatus);
        }

        return ChannelResponse.from(
                channel,
                null,
                privateChannel.participantIds()
        );
    }



    @Override
    public ChannelResponse find(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }

        Channel channel = repository.find(id);

        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널 ID입니다.");
        }

        List<UUID> participantIds =
                readStatusRepository.findByChannelId(id)
                        .stream()
                        .map(ReadStatus::getUserId)
                        .toList();

        return ChannelResponse.from(
                channel,
                null,
                participantIds
        );
    }

    @Override
    public List<ChannelResponse> findAll() {
        return repository.findAll().stream()
                .map(channel -> {
                    List<UUID> participantIds =
                            readStatusRepository.findByChannelId(channel.getId())
                                    .stream()
                                    .map(ReadStatus::getUserId)
                                    .toList();

                    return ChannelResponse.from(
                            channel,
                            null,
                            participantIds
                    );
                })
                .toList();
    }

    @Override
    public ChannelResponse update(UUID id, ChannelRequest.UpdateChannel request) {
        if (id == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }

        if (request == null) {
            throw new IllegalArgumentException("채널 수정 요청은 필수입니다.");
        }

        Channel channel = repository.find(id);

        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널 ID입니다.");
        }

        channel.update(
                request.name(),
                request.description(),
                channel.getType()
        );

        repository.update(channel.getId(), channel);

        List<UUID> participantIds =
                readStatusRepository.findByChannelId(channel.getId())
                        .stream()
                        .map(ReadStatus::getUserId)
                        .toList();

        return ChannelResponse.from(
                channel,
                null,
                participantIds
        );
    }


    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }
        if (repository.find(id) == null) {
            throw new IllegalArgumentException("존재하지 않는 채널 ID입니다.");
        }

        List<ReadStatus> readStatuses =
                readStatusRepository.findByChannelId(id);

        readStatuses.forEach(
                readStatus -> readStatusRepository.delete(readStatus.getId())
        );

        repository.delete(id);
    }
}
