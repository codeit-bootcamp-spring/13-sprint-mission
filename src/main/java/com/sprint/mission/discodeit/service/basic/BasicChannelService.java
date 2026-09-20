package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.projection.ChannelProjection;
import com.sprint.mission.discodeit.dto.projection.UserProjection;
import com.sprint.mission.discodeit.dto.request.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.ChannelTypeException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.mapper.MapperMethod;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.ChannelService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final MapStructMapper mapStructMapper;

    private final MapperMethod mapperMethod;

    private final RoleHierarchy roleHierarchy;

    private final SessionRegistry sessionRegistry;


    /**
     * public 채널 생성
     * @param cpb
     * @return channelDto
     */
    @Override
    @Transactional
    public ChannelDto createPublicChannel(PublicChannelCreateRequest cpb){
        Channel channel = channelRepository.save(new Channel(cpb.name(), cpb.description(), ChannelType.PUBLIC));

        log.debug("ChannelService - public 채널 생성 {}",channel.getId());

        return channelDtoFrom(channel);
    }

    /**
     * private 채널 생성
     * @param cpv
     * @return channelDto
     */
    @Override
    @Transactional
    public ChannelDto createPrivateChannel(PrivateChannelCreateRequest cpv){
        Channel channel = channelRepository.save(new Channel("", "", ChannelType.PRIVATE));

        log.debug("ChannelService - private 채널 생성 {}",channel.getId());

        // todo - request 를 command 레이어를 넣으면서 stream 으로 변경 예정.
        // stream 을 쓰는게 좋다고 했다.
        // 이유는 아마 가독성. 데이터 크기가 커지면 별도 이터레이터로 돌리는 stream 보단 for문을 활용하도록.
        for (UUID pid : cpv.participantIds()){
            User user = getUserOrException(pid);
            readStatusRepository.save(new ReadStatus(user,channel, Instant.now()));

            log.debug("User with id - {} is joined channel",pid);
        }

        return channelDtoFrom(channel);
    }

    /**
     * 유저가 조회 할 수 있는 모든 채널 정보 조회.
     * @param userID UUID
     * @return channelList List
     */
    @Override
    @Transactional
    public List<ChannelDto> findAllByUserID(UUID userID) {

        List<ChannelProjection> channels = new ArrayList<>(channelRepository.getChannelsFromUserId(userID));

        return channels.stream()
                .map(this::getChannelDtoFrom)
                .toList();
    }


    /**
     * id 에 해당하는 public 채널을 업데이트.
     * private 채널이면 에러.
     * @param id UUID
     * @param uci PublicUpdateRequest
     * @return ChannelDto 채널 정보에 대한 반환값.
     */
    @Override
    @Transactional
    public ChannelDto update(UUID id, PublicChannelUpdateRequest uci, Authentication authentication) {


        Channel channel = getChannelOrException(id);

        // 권한검사
        if (channel.getType().equals(ChannelType.PUBLIC))checkAuth(authentication);

        checkPrivateChannel(channel);

        channel.setName(uci.newName());
        channel.setDescription(uci.newDescription());

        return channelDtoFrom(channelRepository.save(channel));
    }

    @Override
    @Transactional
    public void deleteChannel(UUID id,Authentication authentication) {

        Channel channel = getChannelOrException(id);

        // 권한검사
        if(channel.getType().equals(ChannelType.PUBLIC)) checkAuth(authentication);

        channelRepository.deleteById(id);
    }

    // id 에 해당하는 유저를 조회하고 없으면 에러.
    private User getUserOrException(UUID id){
        return userRepository.findById(id).stream().findFirst().orElseThrow(
                () -> new UserNotFoundException("User with id - {} not found",id)
        );
    }

    // 채널 타입이 private 인지 체크
    private void checkPrivateChannel(Channel channel){
        if (channel.getType().equals(ChannelType.PRIVATE)) {
            log.warn("Private channel checked - id : {}, name : {}",channel.getId(), channel.getName());
            throw new ChannelTypeException("Channel with id - {} was private",channel.getId());
        }
    }

    // id 에 해당하는 채널을 조회하고 없으면 에러.
    private Channel getChannelOrException(UUID id){
        return channelRepository.findById(id).orElseThrow(
                () -> new ChannelNotFoundException("Channel with id - {} not found",id)
        );
    }

    // convert ChannelDto from Channel
    private ChannelDto channelDtoFrom(Channel channel){
        ChannelProjection projection = channelRepository.getChannelById(channel.getId())
                .orElseThrow(RuntimeException::new);

        return mapStructMapper.toDto(
                projection,
                getUserDtoFromId(projection.users())
        );
    }

    // convert channel Dto from queried channel info.
    private ChannelDto getChannelDtoFrom(ChannelProjection channel){
        List<UserDto> users = getUserDtoFromId(channel.users());
        return mapStructMapper.toDto(channel, users);
    }

    private List<UserDto> getUserDtoFromId(UUID... userId){

        // public 이라면 user == empty Array
        if (userId == null) return List.of();

        Collection<UserProjection> users = userRepository.getUserInfoFromIds(userId);

        return users.stream().map(
                userProjection ->
                        mapStructMapper.toDto(
                                userProjection,
                                mapStructMapper.toDto(userProjection,mapperMethod), // 임시 사용. 추후 mapperMethod 분리
                                userOnline(userProjection.username())
                        )
        ).toList();
    }


    private void checkAuth(Authentication auth){
        Collection<? extends GrantedAuthority> res = roleHierarchy.getReachableGrantedAuthorities(auth.getAuthorities());

        boolean has = res.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(g -> g.equals("CHANNEL_MANAGER"));

        if (!has) throw new AccessDeniedException("");
    }

    private Boolean userOnline(String username){
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (
                    principal instanceof DiscodeitUserDetails details
                            && details.getUsername().equals(username)
            ){
                return true;
            }
        }
        return false;
    }

}
