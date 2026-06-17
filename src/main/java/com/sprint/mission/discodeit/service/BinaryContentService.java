package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public interface BinaryContentService {

    BinaryContentResponse create(CreateBinaryContentRequest request);

    BinaryContentResponse find(UUID id);

    List<BinaryContentResponse> findAllByIdIn (List<UUID> ids);

    void delete(UUID id);

}
