package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

//ChannelService 구현체
@Service
@RequiredArgsConstructor
//채널 관련 비즈니스 로직을 담당하는 Service 계층
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository; //채널 저장소 객체
  private final ReadStatusRepository readStatusRepository; //읽을 상태 저장소
  private final MessageRepository messageRepository; //메시지 저장소

  @Override //공개 채널 생성
  public ChannelDto create(PublicChannelCreateRequest request) {
    String name = request.getName();
    String description = request.getDescription();
    Channel channel = new Channel(ChannelType.PUBLIC, name, description);
    return channelRepository.save(channel);
  }

  @Override //비공개 채널 생성
  public ChannelDto create(PrivateChannelCreateRequest request) {
    //비공개 채널은 이름/설명이 없음 (필요 시 확장가능)
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    Channel createChannel = channelRepository.save(channel);

    //참여자 목록을 readStatus로 변환
    request.getParticipantIds().stream()
        .map(userId -> new ReadStatus(userId, createChannel.getId(),
            channel.getCreatedAt())) //forEach(readStatusRepository::save);
        .forEach(readStatusRepository::save);
    return createChannel;
  }

  @Override //채널 단건조회 + DTO 변환
  public ChannelDto find(UUID channelId) {
    return channelRepository.findById(channelId).map(this::toDto)
        .orElseThrow(
            () -> new NoSuchElementException("Channel with id " + channelId + " not found"));
  }

  @Override //특정 유저 기준 채널 조회
  public List<ChannelDto> findAllByUserId(UUID userId) {
    //유저가 참여한 비공개 채널 ID 목록
    List<UUID> mySubscribedChannelIds = readStatusRepository.findAllByUserId(userId).stream()
        .map(ReadStatus::getChannelId).toList();
    return channelRepository.findAll().stream()
        .filter(channel -> channel.getType().equals(ChannelType.PUBLIC)
            || mySubscribedChannelIds.contains(channel.getId()))
        .map(this::toDto).toList();
  }

  @Override //채널 수정
  public ChannelDto update(UUID channelId, PublicChannelUpdateRequest request) {
    String newName = request.getNewName();
    String newDecription = request.getNewDescription();
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(
            () -> new NoSuchElementException("Channel with id " + channelId + " not found"));
    if (channel.getType().equals(ChannelType.PRIVATE)) {
      throw new IllegalArgumentException("Private channels can't be updated");
    }
    channel.update(newName, newDecription);
    return channelRepository.save(channel);
  }

  @Override //채널 삭제
  public void delete(UUID channelId) {
    Channel channel = channelRepository.findById(channelId).orElseThrow(
        () -> new NoSuchElementException("Channel with id " + channelId + " not found"));
    messageRepository.deleteAllByChannelId(channel.getId());
    readStatusRepository.deleteAllByChannelId(channel.getId());
    channelRepository.deleteById(channelId);
  }

  //Entity + DTO 변환
  private ChannelDto toDto(Channel channel) {
    // 마지막 메시지 시간 계산. 1.채널 메시지 전체 조회. 2.최신 createdAt 추출
    Instant lastMessageAt = messageRepository.findAllByChannelId(channel.getId())
        .stream().sorted(Comparator.comparing(Message::getCreatedAt).reversed())
        .map(Message::getCreatedAt)
        .limit(1)
        .findFirst()
        .orElse(Instant.MIN);

    //비공개 채널 참여자 목록 구성
    List<UUID> participantIds = new ArrayList<>();
    if (channel.getType().equals(ChannelType.PRIVATE)) {
      readStatusRepository.findAllByChannelId(channel.getId())
          .stream().map(ReadStatus::getUserId)
          .forEach(participantIds::add);
    }

    return new ChannelDto(
        channel.getId(),
        channel.getName(),
        channel.getDescription(),
        channel.getType(),
        lastMessageAt,
        participantIds
    );

  }
}