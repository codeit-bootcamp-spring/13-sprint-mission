package com.sprint.mission.discodeit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageMultipartMapper {

    private final ObjectMapper objectMapper;

    public MessageCreateRequest toCreateRequest(
            MultiValueMap<String, String> formData,
            String messageCreateRequestJson,
            String requestJson,
            String messageRequestJson,
            List<MultipartFile> attachments,
            List<MultipartFile> files
    ) throws IOException {
        List<MultipartFile> multipartFiles = mergeFiles(attachments, files);

        String content = firstNonBlank(
                getFirst(formData, "content"),
                getFirst(formData, "body"),
                getFirst(formData, "text"),
                getFirst(formData, "message")
        );

        UUID authorId = firstNonNull(
                getUuidSafely(getFirst(formData, "authorId")),
                getUuidSafely(getFirst(formData, "userId")),
                getUuidSafely(getFirst(formData, "senderId"))
        );

        UUID channelId = firstNonNull(
                getUuidSafely(getFirst(formData, "channelId")),
                getUuidSafely(getFirst(formData, "roomId"))
        );

        String json = firstNonBlank(
                messageCreateRequestJson,
                requestJson,
                messageRequestJson,
                getFirst(formData, "messageCreateRequest"),
                getFirst(formData, "request"),
                getFirst(formData, "messageRequest")
        );

        if (!isBlank(json)) {
            JsonNode root = objectMapper.readTree(json);

            content = firstNonBlank(
                    content,
                    getText(root, "content"),
                    getText(root, "body"),
                    getText(root, "text"),
                    getText(root, "message")
            );

            authorId = firstNonNull(
                    authorId,
                    getUuidSafely(getText(root, "authorId")),
                    getUuidSafely(getText(root, "userId")),
                    getUuidSafely(getText(root, "senderId")),
                    getUuidSafely(getNestedText(root, "author", "id")),
                    getUuidSafely(getNestedText(root, "user", "id")),
                    getUuidSafely(getNestedText(root, "sender", "id"))
            );

            channelId = firstNonNull(
                    channelId,
                    getUuidSafely(getText(root, "channelId")),
                    getUuidSafely(getText(root, "roomId")),
                    getUuidSafely(getNestedText(root, "channel", "id")),
                    getUuidSafely(getNestedText(root, "room", "id"))
            );
        }

        List<BinaryContentCreateRequest> attachmentRequests =
                toBinaryContentCreateRequests(multipartFiles);

        /*
         * 파일만 보내는 경우 프론트가 content를 빈 문자열로 보낼 수 있다.
         * 기존 Message 엔티티/서비스는 content blank를 허용하지 않으므로
         * 첨부파일이 있으면 기본 문구를 넣어 400 오류를 방지한다.
         */
        if (isBlank(content) && !attachmentRequests.isEmpty()) {
            content = "첨부파일";
        }

        return new MessageCreateRequest(
                content,
                authorId,
                channelId,
                attachmentRequests
        );
    }

    private List<BinaryContentCreateRequest> toBinaryContentCreateRequests(
            List<MultipartFile> files
    ) throws IOException {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        List<BinaryContentCreateRequest> requests = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            BinaryContentCreateRequest request = new BinaryContentCreateRequest(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            );

            requests.add(request);
        }

        return requests;
    }

    private List<MultipartFile> mergeFiles(
            List<MultipartFile> attachments,
            List<MultipartFile> files
    ) {
        List<MultipartFile> merged = new ArrayList<>();

        if (attachments != null && !attachments.isEmpty()) {
            merged.addAll(attachments);
        }

        if (files != null && !files.isEmpty()) {
            merged.addAll(files);
        }

        return merged;
    }

    private String getFirst(MultiValueMap<String, String> formData, String key) {
        if (formData == null || key == null) {
            return null;
        }

        return formData.getFirst(key);
    }

    private String getText(JsonNode root, String fieldName) {
        if (root == null || fieldName == null || !root.has(fieldName)) {
            return null;
        }

        JsonNode node = root.get(fieldName);

        if (node == null || node.isNull()) {
            return null;
        }

        return node.asText();
    }

    private String getNestedText(JsonNode root, String objectName, String fieldName) {
        if (root == null || objectName == null || fieldName == null) {
            return null;
        }

        JsonNode objectNode = root.get(objectName);

        if (objectNode == null || objectNode.isNull()) {
            return null;
        }

        JsonNode fieldNode = objectNode.get(fieldName);

        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }

        return fieldNode.asText();
    }

    private UUID getUuidSafely(String value) {
        if (isBlank(value)) {
            return null;
        }

        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }

        return null;
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }

        for (T value : values) {
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}