package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class PageResponseMapper {

  //Slice (fromSlice)->다음 페이지 있는지만 알면 됨 (무한 스크롤)
  public <T> PageResponse<T> fromSlice(Slice<T> slice, Object nextCursor) {
    return new PageResponse<>(
        slice.getContent(),//현재 페이지의 실제 데이터 목록
        nextCursor,
        slice.getSize(),//한 페이지당 데이터 개수 (50개가 요구사항임.)
        slice.hasNext(),//다음 페이지가 있는지 여부
        0L//전체 데이터 개수 (Slice는 모름)
    );
  }


  //Page (fromPage)-> 전체 몇 개인지도 알아야 함 (1,2,3... 페이지 번호 표시)
  //이번 프로젝트에서는 사용X (확장성을 위한 코드 & 요구사항 충족을 위해 작성함.)
  public <T> PageResponse<T> fromPage(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        null,
        page.getSize(),
        page.hasNext(),
        page.getTotalElements()

    );
  }

}
