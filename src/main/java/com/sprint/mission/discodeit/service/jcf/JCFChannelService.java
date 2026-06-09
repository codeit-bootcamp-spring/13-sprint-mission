package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.*;

public class JCFChannelService implements ChannelService {

    @Override
    public ChannelResponse createPublicChannel(PublicChannelCreateRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelResponse createPrivateChannel(PrivateChannelCreateRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelResponse findById(UUID id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelResponse update(ChannelUpdateRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException();
    }
}
