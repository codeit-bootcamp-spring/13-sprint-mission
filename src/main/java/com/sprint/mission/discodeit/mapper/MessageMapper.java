package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.entity.Message;
import org.mapstruct.Mapper;


import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, BinaryContentMapper.class})
//uses<- {}안에 클래스를 사용한다는 뜻.
//message의 필드 author는 user Entity의 객체를 담음
// -> user를 dto로 변환해야함.: UserMapper.class 필요. (BinaryContentMapper도 동일)
public interface MessageMapper {

  @Mapping(target = "channelId", source = "channel.id")
    //@Mapping(target = "author", source = "author") <- 동일한 필드 명은 자동 매핑됨.
  MessageDto toDto(Message message);

}
