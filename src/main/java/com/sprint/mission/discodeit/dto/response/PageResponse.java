package com.sprint.mission.discodeit.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PageResponse<T> {

  // 페이지네이션 응답을 위해 제네릭 활용
  private List<T> content; // 실제 데이터
  private int number; // 페이지 번호
  private int size; // 페이지 크기
  private boolean hasNext;
  private Long totalElements; // T 데이터의 총 갯수, null일 수 도 있다
}
