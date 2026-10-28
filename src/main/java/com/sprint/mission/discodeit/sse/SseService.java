package com.sprint.mission.discodeit.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 현재 서버에 연결된 SSE 클라이언트의 수명과 실제 전송을 관리한다.
 * 이벤트 생성·저장은 SseEventPublisher, 재접속 이력 조회는 SseMessageReader에 맡긴다.
 * 사용자마다 여러 연결을 허용하며, 한 연결의 실패가 다른 탭의 전송을 중단하지 않게 한다.
 */
@Service
@RequiredArgsConstructor
public class SseService {

    private final SseMessageReader messageReader;
    private final SseConnectionRepository connectionRepository;
    private static final long SSE_TIMEOUT_MILLIS = 60L * 60 * 1000;

    /**
     * 연결을 먼저 등록한 뒤 ping과 이력 복원을 수행한다.
     * lastEventId가 null이면 최초 연결이므로 과거 이벤트를 재생하지 않는다.
     * 초기화에 실패하면 등록한 연결을 제거하고 예외를 전달한다.
     */
    public SseEmitter connect(UUID receiverId, UUID lastEventId) {
        SseConnection connection = registerConnection(receiverId, lastEventId);
        initializeConnection(receiverId, lastEventId, connection);
        return connection.getEmitter();
    }

    /**
     * 저장된 커서의 순번을 초기값으로 사용하며, 커서가 없거나 알 수 없으면 0으로 시작한다.
     * 종료·타임아웃·오류 콜백을 등록해 실패한 연결만 저장소에서 제거한다.
     */
    private SseConnection registerConnection(UUID receiverId, UUID lastEventId) {
        long lastSequence = messageReader.findSequence(lastEventId).orElse(0L);
        SseConnection connection = new SseConnection(new SseEmitter(SSE_TIMEOUT_MILLIS), lastSequence);
        connection.getEmitter().onCompletion(() -> connectionRepository.remove(receiverId, connection));
        connection.getEmitter().onTimeout(() -> connectionRepository.remove(receiverId, connection));
        connection.getEmitter().onError(error -> connectionRepository.remove(receiverId, connection));

        // 이력 조회 전에 등록해야 조회 도중 도착한 실시간 이벤트도 연결의 대기열에 들어간다.
        connectionRepository.add(receiverId, connection);
        return connection;
    }

    /**
     * 지정 사용자의 현재 서버 연결들에 전송한다.
     * 한 탭의 전송 실패는 해당 연결만 제거하고 나머지 연결에는 계속 전송한다.
     */
    public void send(Collection<UUID> receiverIds, SseDelivery delivery) {
        receiverIds.forEach(receiverId -> {
            List<SseConnection> connections = connectionRepository.get(receiverId);

            connections.forEach(connection -> {
                        try {
                            connection.send(delivery);
                        } catch (IOException | RuntimeException e) {
                            connectionRepository.remove(receiverId, connection);
                        }
                    }
            );
        });
    }

    /**
     * 현재 서버에 연결된 모든 사용자에게 전달한다.
     * 전체 사용자용 이력 저장 여부는 이 메서드가 아니라 발행 단계의 broadcast 속성이 결정한다.
     */
    public void broadcast(SseDelivery delivery){
        send(connectionRepository.getAllIds(), delivery);
    }

    /**
     * 15초마다 현재 연결에 ping을 보내고 쓰기가 실패한 연결을 제거한다.
     * 실제 응답 쓰기를 통해 종료를 감지하며 다음 실행은 이번 정리가 끝난 뒤 시작한다.
     */
    @Scheduled(fixedDelay = 15_000)
    public void cleanUp() {
        connectionRepository.getAll()
                .forEach((receiverId, connections)->
                    connections.stream()
                            .filter(connection -> !ping(connection.getEmitter()))
                            .forEach(connection -> connectionRepository.remove(receiverId, connection))
                    );

    }

    /**
     * SSE 주석 프레임을 보내 연결을 확인한다.
     * 이벤트 ID를 보내지 않으므로 클라이언트의 마지막 업무 이벤트 커서를 바꾸지 않는다.
     */
    private boolean ping(SseEmitter sseEmitter) {
        try {
            sseEmitter.send(SseEmitter.event().comment("ping"));
            return true;
        } catch (IOException | IllegalStateException e) {
            return false;
        }

    }

    private void initializeConnection(UUID receiverId, UUID lastEventId, SseConnection connection){
        try {
            if (!ping(connection.getEmitter())){
                throw new IllegalStateException("SSE 연결 확인에 실패했습니다.");
            }
            List<SseDelivery> deliveries = lastEventId == null ? List.of() : messageReader.findDeliveriesAfter(receiverId, lastEventId);
            connection.completeReplay(deliveries);
        } catch (IOException | RuntimeException e) {
            connectionRepository.remove(receiverId, connection);
            throw new IllegalStateException("SSE 연결 초기화 또는 이벤트 복원에 실패했습니다.", e);
        }
    }

}
