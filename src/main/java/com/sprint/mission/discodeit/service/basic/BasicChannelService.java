package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor // 생성자를 @RequiredArgsConstructor로 대체
@Service // Basic*Service 구현체를 Service 인터페이스의 Bean으로 등록
@Transactional(readOnly = true)
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelMapper channelMapper;
  // 매퍼 객체를 사용하기 위해 의존성 주입

  // PRIVATE, PUBLIC 채널 생성 메소드 분리
  // PUBLIC 채널 생성할 때는 기존 로직 유지
  @Transactional
  @Override
  public ChannelDto createPublicChannel(PublicChannelCreateRequest request) {
    Channel channel = new Channel(ChannelType.PUBLIC, request.getName(), request.getDescription());
    Channel saved = channelRepository.save(channel);
    log.info("공개 채널 생성 name={}, description={}", request.getName(), request.getDescription());
    return channelMapper.toDto(saved);
  }

  // PRIVATE 채널 생성할 때 채널에 참여하는 User 정보 받아 User 별 ReadStatus 정보 생성 (name, description 속성 생략)
  @Transactional
  @Override
  public ChannelDto createPrivateChannel(PrivateChannelCreateRequest request) {
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    Channel createdChannel = channelRepository.save(channel);
    // 채널 생성
    List<ReadStatus> readStatuses = userRepository.findAllById(
            request.getParticipantIds()).stream()
        .map(user -> new ReadStatus(user, createdChannel, createdChannel.getCreatedAt()))
        .toList();// 초기 시간값 설정 로직 변경
    readStatusRepository.saveAll(readStatuses); // saveAll로 한 번에 저장
    log.info("비공개 채널 생성 participantIds={}", request.getParticipantIds());
    return channelMapper.toDto(createdChannel);
  }

  @Transactional(readOnly = true)
  @Override
  public ChannelDto find(UUID channelId) {
    log.debug("채널 조회 channelId={}", channelId);
    return channelRepository.findById(channelId)
        .map(channel -> channelMapper.toDto(channel))
        .orElseThrow(
            () -> new ChannelNotFoundException(channelId));
  }

  @Transactional(readOnly = true)
  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) { // 특정 User가 볼 수 있는 Channel 목록 조회
    List<UUID> participantsId = readStatusRepository.findByUserId(userId).stream()
        .map(readStatus -> readStatus.getChannel().getId())
        .toList(); // channel
    log.debug("특정 사용자 채널 목록 조회 userId={}", userId);
    // PUBLIC: 전체 조회, PRIVATE: User 참여 채널 조회
    return channelRepository.findAll().stream()
        .filter(channel -> channel.getType() == ChannelType.PUBLIC || participantsId.contains(
            channel.getId()))
        .map(channelMapper::toDto).toList();
  }

  @Transactional
  @Override
  public ChannelDto update(UUID channelId, PublicChannelUpdateRequest request) {
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new ChannelNotFoundException(channelId)); // 제공된 API 스펙과 맞추어 수정
    // PRIVATE 채널은 수정 불가능
    if (channel.getType() == ChannelType.PRIVATE) {
      log.warn("비공개 채널은 수정 불가능 channelType={}", channel.getType());
      throw new PrivateChannelUpdateException(channelId); // 제공된 API 스펙과 맞추어 수정
    }
    channel.update(request.getNewName(), request.getNewDescription());
    log.info("공개 채널 정보 수정 channelId={}, newName={}", channelId, request.getNewName());
    return channelMapper.toDto(channel);
  }

  @Transactional
  @Override
  public void delete(UUID channelId) {
    if (!channelRepository.existsById(channelId)) {
      log.warn("존재하지 않는 채널 아이디 {}", channelId);
      throw new ChannelNotFoundException(channelId);
    }
    // 관련된 도메인도 삭제: message, readStatus
    messageRepository.findByChannelId(channelId)
        .forEach(message -> messageRepository.deleteById(message.getId()));
    readStatusRepository.findByChannelId(channelId)
        .forEach(readStatus -> readStatusRepository.deleteById(readStatus.getId()));
    channelRepository.deleteById(channelId);
    log.info("채널 삭제 channelId={}", channelId);
  }
}
