package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.*;
import org.springframework.core.io.Resource;
import org.springframework.http.*;

import java.io.*;
import java.util.*;

public interface BinaryContentStorage {

    UUID put(UUID binaryContentId, byte[] bytes);

    InputStream get(UUID binaryContentId);

    ResponseEntity<Resource> download(BinaryContentResponse response);
}

