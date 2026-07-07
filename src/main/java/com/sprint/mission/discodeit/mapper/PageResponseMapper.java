package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PageResponseMapper {

    public PageResponse<MessageDto> fromSlice(Slice<MessageDto> slice) {
        Instant nextCursor = slice.getContent().isEmpty()
                ? null
                : slice.getContent().get(slice.getNumberOfElements() - 1).createdAt();

        return new PageResponse<>(
                slice.getContent(),
                nextCursor,
                slice.getSize(),
                slice.hasNext(),
                null
        );
    }

    public void fromPage(Page<MessageDto> page) {
    }

}
