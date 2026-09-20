package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;

@Mapper(componentModel = "spring", uses = {BinaryContentMapper.class})
public abstract class UserMapper {

  @Autowired
  private SessionRegistry sessionRegistry;

  @Mapping(target = "online", expression = "java(resolveOnline(user))")
  public abstract UserDto toDto(User user);

  /**
   * UserStatus 엔티티 대신 SessionRegistry에 등록된 세션 유무로 접속 여부를 판단한다.
   */
  protected boolean resolveOnline(User user) {
    return sessionRegistry.getAllPrincipals().stream()
        .filter(DiscodeitUserDetails.class::isInstance)
        .map(DiscodeitUserDetails.class::cast)
        .anyMatch(principal -> principal.getUserDto().id().equals(user.getId())
            && !sessionRegistry.getAllSessions(principal, false).isEmpty());
  }
}
