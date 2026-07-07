package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class PageResponseMapper {
    public <T> PageResponse<T> fromSlice(Slice<T> slc){
        return new PageResponse<>(
                slc.getContent()
                , slc.getNumber()
                , slc.getSize()
                , slc.hasNext()
                , null
        );
    }
    public <T> PageResponse<T> fromPage(Page<T> pg){
        return new PageResponse<>(
                pg.getContent()
                , pg.getNumber()
                , pg.getSize()
                , pg.hasNext()
                , pg.getTotalElements()
        );
    }
}
