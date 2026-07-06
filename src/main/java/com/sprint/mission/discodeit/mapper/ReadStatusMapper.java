package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@NoArgsConstructor
public class ReadStatusMapper {

    public ReadStatusDto toDto(ReadStatus readStatus) {
        return new ReadStatusDto(
                readStatus.getId()
                ,readStatus.getUser().getId()
                ,readStatus.getChannel().getId()
                ,readStatus.getLastReadAt()
        );
    }

}
