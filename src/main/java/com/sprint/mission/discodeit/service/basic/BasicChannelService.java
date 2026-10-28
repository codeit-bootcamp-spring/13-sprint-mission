package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.projection.ChannelProjection;
import com.sprint.mission.discodeit.dto.request.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.event.channel.ChannelEvent;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.ChannelTypeException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.EntityMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.CertificationChecker;
import com.sprint.mission.discodeit.service.ChannelService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.sprint.mission.discodeit.config.cache.DiscodeItCacheConfig.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicChannelService implements ChannelService {

    private final String SERVICE_NAME = "ChannelService";

    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;

    private final CertificationChecker certificationChecker;

    private final EntityMapper entityMapper;

    private final RoleHierarchy roleHierarchy;

    private final ApplicationEventPublisher eventPublisher;

    private final CacheManager cacheManager;

    /**
     * public 채널 생성
     * @param publicChannelCreate 채널 생성 요청 정보.
     * @return channelDto
     */
    @Override
    @CacheEvict(cacheNames = CHANNEL_CACHE, allEntries = true)
    @Transactional
    public ChannelDto createPublicChannel(PublicChannelCreateRequest publicChannelCreate){
        Channel channel = channelRepository.save(
                new Channel(
                        publicChannelCreate.name(),
                        publicChannelCreate.description(),
                        ChannelType.PUBLIC)
        );

        log.debug("ChannelService - public 채널 생성 {}",channel.getId());

        ChannelDto dto = getChannelDtoFromChannel(channel);

        eventPublisher.publishEvent(new ChannelEvent("channels.created",dto));

        return dto;
    }

    /**
     * private 채널 생성
     * @param publicChannelCreate 채널 생성 요청 정보. List - UUID 유저 id 정보.
     * @return channelDto
     */
    @Override
    @CacheEvict(cacheNames = CHANNEL_CACHE, allEntries = true)
    @Transactional
    public ChannelDto createPrivateChannel(PrivateChannelCreateRequest publicChannelCreate){
        Channel channel = channelRepository.save(new Channel(null, null, ChannelType.PRIVATE));

        log.debug("ChannelService - private 채널 생성 {}",channel.getId());


        publicChannelCreate.participantIds().forEach(
                userId -> {
                    User user = getUserOrException(userId);
                    readStatusRepository.save(new ReadStatus(user,channel, Instant.now()));

                    log.debug("User with id - {} is joined channel",userId);
                }
        );

        ChannelDto dto = getChannelDtoFromChannel(channel);

        eventPublisher.publishEvent(new ChannelEvent("channels.created",dto));

        return dto;

    }

    /**
     * 유저가 조회 할 수 있는 모든 채널 정보 조회.
     * @param userId UUID
     * @return channelList List
     */
    @Override
    @Cacheable(value = CHANNEL_CACHE, key = "#userId", unless = "#result.isEmpty()")
    @Transactional
    public List<ChannelDto> findAllByUserID(UUID userId) {
        Map<UUID,ChannelProjection> channelMap = channelRepository.getChannelsFromUserId(userId);

        // user List from queried channel has.
        List<UUID> userIdList = channelMap.values().stream()
                .map(ChannelProjection::userIds)
                .flatMap(Collection::stream)
                .toList();

        // Map<userid, userDto> - set UserDto for construct channelDto
        Map<UUID, UserDto> userDtos = getUserDtoFromUserIdList(userIdList);

        log.debug(userDtos.toString());

        // convert channel projection to channelDto
        return channelMap.values().stream()
                .map(
                        channelProjection -> entityMapper.toDto(
                                channelProjection,
                                channelProjection.userIds().stream().map(userDtos::get).toList()
                        )
                ).toList();
    }


    /**
     * id 에 해당하는 public 채널을 업데이트.
     * private 채널이면 에러.
     * @param id UUID
     * @param uci PublicUpdateRequest
     * @return ChannelDto 채널 정보에 대한 반환값.
     */
    @Override
    @CacheEvict(cacheNames = CHANNEL_CACHE, allEntries = true)
    @Transactional
    public ChannelDto update(UUID id, PublicChannelUpdateRequest uci, Authentication authentication) {
        Channel channel = getChannelOrException(id);

        checkUpdatable(channel, authentication);

        // update and save
        channel.update(
                uci.newName(),
                uci.newDescription()
        );
        channelRepository.save(channel);

        ChannelDto dto = getChannelDtoFromChannel(channel);

        eventPublisher.publishEvent(new ChannelEvent("channels.updated",dto));

        return dto;

    }

    /**
     * 채널 삭제 매서드
     * @param id 채널 Id.
     * @param authentication 현재 인증 사용자 인가정보.
     */
    @Override
    @CacheEvict(cacheNames = CHANNEL_CACHE, allEntries = true)
    @Transactional
    public void deleteChannel(UUID id,Authentication authentication) {

        Channel channel = getChannelOrException(id);

        // 퍼블릭 채널이라면 사용자 인가 검정
        if(channel.getType().equals(ChannelType.PUBLIC)) checkAuth(authentication);
        // todo - 프라이베이트 라면, 채널 소유자 인지 체크?

        ChannelDto dto = getChannelDtoFromChannel(channel);

        // todo - channel 에 속한 message 전부 삭제.
        channelRepository.deleteById(id);

        eventPublisher.publishEvent(new ChannelEvent("channels.deleted",dto));
    }


    // id 에 해당하는 유저를 조회하고 없으면 에러.
    private User getUserOrException(UUID id){
        return userRepository.findById(id).stream().findFirst().orElseThrow(
                () -> new UserNotFoundException("User with id - {} not found",id)
        );
    }

    // 채널 타입이 private 인지 체크
    private void checkUpdatable(Channel channel, Authentication authentication){
        if (channel.getType().equals(ChannelType.PRIVATE)) {
            log.warn("Private channel checked - id : {}, name : {}",channel.getId(), channel.getName());
            throw new ChannelTypeException("Channel with id - %s was private",channel.getId());
        } else
            checkAuth(authentication);
    }

    // id 에 해당하는 채널을 조회하고 없으면 에러.
    private Channel getChannelOrException(UUID id){
        return channelRepository.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException(
                        "%s - Channel with id - %s not found",
                        SERVICE_NAME,
                        id
                ));
    }

    // 현재 세션 사용자의 인가 체크. (channel manager)
    private void checkAuth(Authentication auth){
        Collection<? extends GrantedAuthority> res = roleHierarchy
                .getReachableGrantedAuthorities(auth.getAuthorities());

        res.forEach(grantedAuthority -> {log.debug("[{}] - 권한 {}",SERVICE_NAME,grantedAuthority.getAuthority());});

        boolean has = res.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(g -> g.equals("ROLE_CHANNEL_MANAGER")); // Auth 안이라 preString 붙음?

        log.debug("[{}] - 사용자 권한 체크 - user : {}, has manager : {}", SERVICE_NAME, auth.getName(), has);

        if (!has) throw new DiscodeitException(
                ExceptionCode.INVALID_AUTH_ERROR,
                "[%s] - 사용자 권한 인가 거부. user : %s",
                SERVICE_NAME,
                auth.getName()
        );
    }

    /*
    채널 Dto 변환 매서드. 단건.
     */
    private ChannelDto getChannelDtoFromChannel(Channel channel){
        ChannelProjection channelProjection = channelRepository.getChannelById(channel.getId())
                .orElseThrow(() -> new ChannelNotFoundException(
                        "{} -  Channel not Found by id - {}",
                        SERVICE_NAME,
                        channel.getId()
                ));

        List<UserDto> users = getUserDtoFromUserIdList(channelProjection.userIds()).values().stream().toList();

        return entityMapper.toDto(
                channelProjection,
                users
        );
    }

    // 채널에 포함된 유저 정보 반환 -> Map<유저 id, UserDto>
    private Map<UUID, UserDto> getUserDtoFromUserIdList(List<UUID> userIdList){
        List<User> users = userRepository.findAllById(userIdList);

        return users.stream()
                .map(user -> entityMapper.toDto(user, certificationChecker.online(user.getId())))
                .collect(Collectors.toMap(
                        UserDto::id,
                        dto -> dto
                ));
    }

}
