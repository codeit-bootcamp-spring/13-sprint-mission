package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;


@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name="discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
public class JCFChannelRepository implements ChannelRepository {
    private final HashMap<UUID, Channel> data = new HashMap<>();

    @Override
    public void save(Channel cnl) {
        data.put(cnl.getId(),cnl);
    }

    @Override
    public List<Channel> find (Predicate<Channel> fn) {
        return data.values().stream()
                .filter(fn)
                .toList();
    }

    @Override
    public List<Channel> findAll() {
        return find(cnl -> true);
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        return find(cnl -> cnl.getId().equals(id)).stream().findFirst();
    }

    @Override
    public void delete(UUID channel) {
        data.remove(channel);
    }

}
