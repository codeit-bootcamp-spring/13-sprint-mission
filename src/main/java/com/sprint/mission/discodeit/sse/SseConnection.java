package com.sprint.mission.discodeit.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * 하나의 SSE 연결에서 복원 중인 이력과 새로 도착한 이벤트를 합쳐 전송한다.
 * sequence는 저장 순서를 나타내고, eventId는 클라이언트가 재접속할 때 돌려주는 식별자다.
 * synchronized는 이 JVM의 이 연결만 보호한다. 분산 잠금이나 종단 간 전달 보장은 아니다.
 */
public class SseConnection {

    private final SseEmitter emitter;

    private final NavigableMap<Long, SseDelivery> pendingDeliveries = new TreeMap<>();

    private long lastSentSequence;
    private boolean replaying =true;
    private boolean closed = false;

    public SseConnection(SseEmitter emitter, long lastSentSequence){
        this.emitter = emitter;
        this.lastSentSequence = lastSentSequence;
    }

    public SseEmitter getEmitter() {
        return emitter;
    }

    /**
     * 복원 중에는 실시간 이벤트를 순번별로 대기시키고, 복원 후에는 바로 전송한다.
     * 이미 보낸 순번 이하의 이벤트는 무시하므로 늦게 도착한 과거 이벤트도 전송되지 않는다.
     */
    public synchronized void send(SseDelivery delivery) throws IOException {
        if (closed || delivery.sequence() <= lastSentSequence){
            return;
        }

        if (replaying){
            pendingDeliveries.putIfAbsent(
                    delivery.sequence(),
                    delivery
            );
            return;
        }
        sendNow(delivery);
    }

    /**
     * 복원 이력과 그동안 대기한 실시간 이벤트를 같은 TreeMap에 합쳐 순번대로 전송한다.
     * 같은 순번은 한 번만 대기시키며 완료 후 실시간 전송 모드로 전환한다.
     * 이미 닫혔거나 복원을 끝낸 연결에서는 다시 실행할 수 없다.
     */
    public synchronized void completeReplay(Collection<SseDelivery> replayedDeliveries) throws IOException {
        if (closed){
            throw new IllegalStateException("이미 종료된 SSE 연결입니다.");
        }

        if (!replaying){
            throw new IllegalStateException("SSE 이벤트 복원이 이미 완료되었습니다.");
        }

        replayedDeliveries.stream()
                .filter(delivery -> delivery.sequence() > lastSentSequence)
                .forEach(delivery -> pendingDeliveries.putIfAbsent(delivery.sequence(), delivery));

        while (!pendingDeliveries.isEmpty()){
            SseDelivery delivery = pendingDeliveries.pollFirstEntry().getValue();
            sendNow(delivery);
        }

        replaying = false;
    }

    /**
     * 추가 전송을 막고 대기 이벤트를 비운다.
     * SseEmitter의 HTTP 응답을 직접 complete하는 메서드는 아니다.
     */
    public synchronized void close(){
        closed = true;
        pendingDeliveries.clear();
    }

    private void sendNow(SseDelivery delivery) throws IOException {
        if (closed || delivery.sequence() <= lastSentSequence){
            return;
        }

        try {
            emitter.send(
                    SseEmitter.event()
                            .id(delivery.eventId().toString())
                            .name(delivery.eventName())
                            .data(delivery.data())
            );
            // 쓰기 호출이 성공한 뒤에만 진행 위치를 옮긴다. 클라이언트의 처리 완료 확인을 뜻하지는 않는다.
            lastSentSequence = delivery.sequence();
        } catch (IOException | RuntimeException e) {
            close();
            throw e;
        }
    }
}
