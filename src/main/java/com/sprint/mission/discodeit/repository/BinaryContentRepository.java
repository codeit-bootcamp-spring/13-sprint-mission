package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface BinaryContentRepository extends JpaRepository<BinaryContent, UUID> {

//    void createBinaryContent(BinaryContent binaryContent);
//    Optional<BinaryContent> findBinaryContentById(UUID binaryContentId);
//    List<BinaryContent> findAllBinaryContentByIdIn(List<UUID> binaryContentIds);
//    void deleteBinaryContent(UUID id);

    List<BinaryContent> findAllByIdIn(List<UUID> uuidList);


}
