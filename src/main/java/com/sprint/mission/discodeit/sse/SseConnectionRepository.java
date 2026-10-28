package com.sprint.mission.discodeit.sse;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 사용자 ID별로 이 서버가 보유한 SSE 연결들을 관리한다.
 * 연결은 실제 HTTP 응답을 포함하므로 Redis에 공유하지 않고 각 인스턴스 메모리에 둔다.
 * Map과 내부 List 모두 동시 접근을 지원하여 다중 탭의 연결·종료·순회를 처리한다.
 */
@Repository
public class SseConnectionRepository {

    private final Map<UUID, List<SseConnection>> connections = new ConcurrentHashMap<>();

    public void add(UUID receiverId, SseConnection connection){
        connections.compute(receiverId, (id, list)->{
            if (list == null){
                list = new CopyOnWriteArrayList<>();
            }
            list.add(connection);
            return list;
        });
    }

    /**
     * 연결을 먼저 닫아 추가 전송을 차단한 뒤 사용자 목록에서 제거한다.
     * 마지막 탭이 사라지면 사용자 키도 제거하며 이미 제거된 연결에 대한 호출도 허용한다.
     */
    public void remove(UUID receiverId, SseConnection connection){
        connection.close();

        connections.computeIfPresent(receiverId, (id, list)->{
            list.remove(connection);
            return list.isEmpty() ? null : list;
        });
    }

    public List<SseConnection> get(UUID receiverId){
        return List.copyOf(connections.getOrDefault(receiverId, List.of()));
    }

    /**
     * 외부 순회 중 내부 목록을 변경하지 않도록 사용자별 목록을 복사해 반환한다.
     * 전체 Map을 잠근 단일 시점 스냅샷은 아니며 복사 도중 새 연결이 추가될 수 있다.
     */
    public Map<UUID, List<SseConnection>> getAll(){
        Map<UUID, List<SseConnection>> snapshot = new ConcurrentHashMap<>();

        connections.forEach((receiverId, list)->snapshot.put(receiverId, List.copyOf(list)));

        return Map.copyOf(snapshot);
    }

    public Set<UUID> getAllIds(){
        return Set.copyOf(connections.keySet());
    }
}
