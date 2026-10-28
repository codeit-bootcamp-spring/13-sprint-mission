package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.config.CacheConfig;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.user.UserLoginFailedException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReader {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CacheManager cacheManager;
    private final UserOnlineStatusReader userOnlineStatusReader;

    public Optional<UserDto> findUserDto(UUID userId) {
        return Optional.ofNullable(findUserDtosByIds(List.of(userId)).get(userId));
    }

    public Map<UUID, UserDto> findUserDtosByIds(Collection<UUID> userIds) {
        if (userIds.isEmpty()) return Map.of();

        Cache cache = Objects.requireNonNull(cacheManager.getCache(CacheConfig.USERS), "사용자 캐시가 설정되지 않았습니다.");
        Map<UUID, UserDto> users = new HashMap<>();
        List<UUID> missingIds = new ArrayList<>();
        for (UUID userId : new LinkedHashSet<>(userIds)) {
            UserDto cached = cache.get(userId, UserDto.class);
            if (cached == null) missingIds.add(userId);
            else users.put(userId, cached);
        }

        if (!missingIds.isEmpty()) {
            for (User user : userRepository.findByIdIn(missingIds)) {
                UserDto dto = userMapper.toDto(user,false);
                cache.put(dto.id(), dto);
                users.put(dto.id(), dto);
            }
        }

        if (users.isEmpty()){
            return Map.of();
        }

        Set<UUID> onlineUserIds = userOnlineStatusReader.findOnlineUserIds(users.keySet());

        users.replaceAll((userId, user) -> withOnline(user, onlineUserIds.contains(userId)));

        return users;
    }

    public List<UUID> findUserIdsByRole(Role role) {
        return userRepository.findUserIdsByRole(role);
    }

    public boolean isUserExist(UUID userId) {
        return userRepository.existsById(userId);
    }

    public User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(UserLoginFailedException::new);
    }

    public User getReferenceUser(UUID userId) {
        if (!isUserExist(userId)) {
            throw new UserNotFoundException(userId);
        }
        return userRepository.getReferenceById(userId);
    }

    /**
     * 프로필 파일의 소유자 ID만 조회한다.
     * 해당 프로필을 쓰는 사용자가 없으면 빈 Optional을 반환해 잘못된 개인 방송을 방지한다.
     */
    public Optional<UUID> getUserIdFromProfile(UUID profileId) {
        return userRepository.findUserIdByProfileId(profileId);
    }

    private UserDto withOnline(UserDto user, boolean online) {
        return new UserDto(
                user.id(),
                user.username(),
                user.email(),
                user.profile(),
                online,
                user.role(),
                user.createdAt(),
                user.updatedAt()
        );
    }
}
