package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final ChannelMapper channelMapper;
  private final UserRepository userRepository;

  @Override
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @Transactional
  public ChannelDto createPublic(String name, String description) {
    log.info("PUBLIC 채널 생성 요청 - name: {}", name);

    ChannelDto result = channelMapper.toDto(
        channelRepository.save(
            new Channel(ChannelType.PUBLIC, name, description)));

    log.info("PUBLIC 채널 생성 완료 - channelId: {}", result.id());
    return result;
  }

  @Override
  @Transactional
  public ChannelDto createPrivate(List<UUID> participantIds) {
    log.info("PRIVATE 채널 생성 요청 - 참여자 수: {}", participantIds.size());

    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    channelRepository.save(channel);
    participantIds.forEach(userId -> {

      User user = userRepository.findById(userId)
          .orElseThrow(() -> new UserNotFoundException(userId));
      readStatusRepository.save(new ReadStatus(user, channel, Instant.now()));

    }); //Instant.now(): 현재 시간을 Instant 타입으로 반환하는 메서드.

    log.info("PRIVATE 채널 생성 완료 - channelId: {}", channel.getId());
    return channelMapper.toDto(channel);
  }

  @Override
  @Transactional(readOnly = true)
  public ChannelDto find(UUID channelId) {
    log.info("채널 단건 조회 요청 - channelId: {}", channelId);

    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new ChannelNotFoundException(channelId));

    log.info("채널 단건 조회 완료 - channelId: {}", channelId);

    return channelMapper.toDto(channel);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ChannelDto> findAllByUserId(UUID userId) {
    log.info("유저의 채널 목록 조회 요청 - userId: {}", userId);

    List<UUID> participatingChannelID = readStatusRepository.findAllByUser_Id(userId).stream()
        .map(readStatus -> readStatus.getChannel().getId())
        .toList();

    List<Channel> all = channelRepository.findAll()
        .stream().filter(channel -> channel.getType() == ChannelType.PUBLIC ||
            participatingChannelID.contains(channel.getId()))
        .toList();

    log.info("채널 목록 조회 완료 - 채널 수: {}", all.size());

    return all.stream().map(channelMapper::toDto).toList();
  }

  @Override
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @Transactional
  public ChannelDto update(UUID channelId, String newName, String newDescription) {
    log.info("채널 수정 요청 - channelId: {}", channelId);

    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new ChannelNotFoundException(channelId));
    if (channel.getType() == ChannelType.PRIVATE) {
      throw new PrivateChannelUpdateException(channelId);
    }
    channel.update(newName, newDescription);

    log.info("채널 수정 완료 - channelId: {}", channelId);

    return channelMapper.toDto(channel);
  }

  @Override
  @Transactional
  public void delete(UUID channelId) {
    log.info("채널 삭제 요청 - channelId: {}", channelId);

    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new ChannelNotFoundException(channelId));

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    DiscodeitUserDetails userDetails = (DiscodeitUserDetails) authentication.getPrincipal();
    UUID currentUserId = userDetails.getUserDto().id();

    if (channel.getType() == ChannelType.PUBLIC) {
      boolean isManager = authentication.getAuthorities().stream()
          .anyMatch(a -> a.getAuthority()
              .equals("ROLE_CHANNEL_MANAGER") || a.getAuthority().equals("ROLE_ADMIN"));

      if (!isManager) {
        throw new AccessDeniedException("채널 관리자만 삭제 할 수 있습니다.");
      }
    } else {
      boolean isParticipant = readStatusRepository.existsByUser_IdAndChannel_Id(currentUserId,
          channelId);
      if (!isParticipant) {
        throw new AccessDeniedException("본인이 참여한 채널만 삭제할 수 있습니다.");
      }
    }
    channelRepository.deleteById(channelId);

    log.info("채널 삭제 완료 - channelId: {}", channelId);
    //ON DELETE CASCADE 부모행 삭제-> 자식(messages, read_statuses)행 자동 삭제
  }
}