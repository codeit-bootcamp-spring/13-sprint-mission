package com.sprint.mission.discodeit.storage;

import java.util.UUID;

public interface BinaryContentStorage {

    void put(UUID id, byte[] bytes);

    byte[] get(UUID id);

    void delete(UUID id);
}