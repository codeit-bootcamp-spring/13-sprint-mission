package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class JCFChannelRepository implements ChannelRepository {
    private final HashMap<UUID, Channel> data;

    private static class JCR {
        private static final JCFChannelRepository INSTANCE = new JCFChannelRepository();
    }

    private JCFChannelRepository() {
        data = new HashMap<>();
    }

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
    public void delete(UUID channel) {
        data.remove(channel);
    }

}
