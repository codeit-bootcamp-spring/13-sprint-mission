package com.sprint.mission.discodeit.sse;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class SseEmitterRepository {
    private final ConcurrentMap<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>();


    public SseEmitter register(UUID receiver, SseEmitter emitter) {
        // 멀티 스레드 동시작업이 있으므로 COW Array 를 사용한다.
        data.computeIfAbsent(receiver, id -> new CopyOnWriteArrayList<>()).add(emitter);
        return emitter;
    }

    public List<SseEmitter> findByReceiverList(Collection<UUID> receiverIds) {
        List<SseEmitter> result = new ArrayList<>();
        for(UUID id : receiverIds) {
            if (data.get(id) != null)
                result.addAll(data.get(id));
        }
        return result;
    }

    public List<SseEmitter> findAll(){
        return data.values().stream().flatMap(List::stream).toList();
    }

    public void delete(UUID receiver,SseEmitter emitter) {
        List<SseEmitter> emitters = data.get(receiver);
        emitters.forEach(em -> {
            if (em == emitter) {
                em.complete();  // 삭제될 emitter 은 반환 끊음.
                emitters.remove(em);
            }
        });
    }

    public void delete(SseEmitter emitter){
        for (UUID receiver : data.keySet()) {
            List<SseEmitter> emitters = data.get(receiver);
            if (emitters.contains(emitter)){
                emitters.remove(emitter);
                return;
            }
        }
    }

}

