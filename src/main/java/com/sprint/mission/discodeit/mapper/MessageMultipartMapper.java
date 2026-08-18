package com.sprint.mission.discodeit.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageMultipartRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageMultipartMapper {

    private final ObjectMapper objectMapper;

    public MessageCreateRequest toCreateRequest(
            MessageMultipartRequest multipartRequest
    ) throws IOException {

        if (multipartRequest == null) {
            throw new IllegalArgumentException(
                    "multipart 메시지 생성 요청은 필수입니다."
            );
        }

        String content = firstNonBlank(
                multipartRequest.getContent(),
                multipartRequest.getBody(),
                multipartRequest.getText(),
                multipartRequest.getMessage()
        );

        String authorIdValue = firstNonBlank(
                multipartRequest.getAuthorId(),
                multipartRequest.getUserId(),
                multipartRequest.getSenderId()
        );

        String channelIdValue = firstNonBlank(
                multipartRequest.getChannelId(),
                multipartRequest.getRoomId()
        );

        /*
         * 프론트에서 messageCreateRequest를 JSON Blob으로 보내는 경우 처리.
         * request / messageRequest 문자열 방식도 기존 호환성을 위해 유지.
         */
        String messageCreateRequestJson =
                readMultipartJson(
                        multipartRequest.getMessageCreateRequest()
                );

        String json = firstNonBlank(
                messageCreateRequestJson,
                multipartRequest.getRequest(),
                multipartRequest.getMessageRequest()
        );

        if (!isBlank(json)) {
            JsonNode root = readJson(json);

            content = firstNonBlank(
                    content,
                    getText(root, "content"),
                    getText(root, "body"),
                    getText(root, "text"),
                    getText(root, "message")
            );

            authorIdValue = firstNonBlank(
                    authorIdValue,
                    getText(root, "authorId"),
                    getText(root, "userId"),
                    getText(root, "senderId"),
                    getNestedText(root, "author", "id"),
                    getNestedText(root, "user", "id"),
                    getNestedText(root, "sender", "id")
            );

            channelIdValue = firstNonBlank(
                    channelIdValue,
                    getText(root, "channelId"),
                    getText(root, "roomId"),
                    getNestedText(root, "channel", "id"),
                    getNestedText(root, "room", "id")
            );
        }

        UUID authorId = parseUuid(
                authorIdValue,
                "authorId"
        );

        UUID channelId = parseUuid(
                channelIdValue,
                "channelId"
        );

        List<MultipartFile> multipartFiles = mergeFiles(
                multipartRequest.getAttachments(),
                multipartRequest.getFiles()
        );

        List<BinaryContentCreateRequest> attachmentRequests =
                toBinaryContentCreateRequests(multipartFiles);

        return new MessageCreateRequest(
                content,
                authorId,
                channelId,
                attachmentRequests
        );
    }

    /*
     * multipart/form-data에서 JSON Blob/File로 전달된
     * messageCreateRequest의 내용을 문자열로 변환한다.
     */
    private String readMultipartJson(
            MultipartFile file
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            return null;
        }

        String json = new String(
                file.getBytes(),
                StandardCharsets.UTF_8
        );

        return isBlank(json)
                ? null
                : json.trim();
    }

    private JsonNode readJson(
            String json
    ) {
        try {
            return objectMapper.readTree(json);

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "메시지 생성 요청 JSON 형식이 올바르지 않습니다.",
                    e
            );
        }
    }

    private List<BinaryContentCreateRequest>
    toBinaryContentCreateRequests(
            List<MultipartFile> files
    ) throws IOException {

        if (files == null || files.isEmpty()) {
            return List.of();
        }

        List<BinaryContentCreateRequest> requests =
                new ArrayList<>();

        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) {
                continue;
            }

            String fileName = resolveFileName(
                    file.getOriginalFilename()
            );

            String contentType = resolveContentType(
                    file.getContentType()
            );

            requests.add(
                    new BinaryContentCreateRequest(
                            fileName,
                            contentType,
                            file.getBytes()
                    )
            );
        }

        return List.copyOf(requests);
    }

    private List<MultipartFile> mergeFiles(
            List<MultipartFile> attachments,
            List<MultipartFile> files
    ) {

        List<MultipartFile> merged = new ArrayList<>();

        if (attachments != null) {
            merged.addAll(attachments);
        }

        if (files != null) {
            merged.addAll(files);
        }

        return merged;
    }

    private String getText(
            JsonNode root,
            String fieldName
    ) {

        if (root == null
                || fieldName == null
                || !root.has(fieldName)) {
            return null;
        }

        JsonNode node = root.get(fieldName);

        if (node == null
                || node.isNull()
                || node.isContainerNode()) {
            return null;
        }

        String value = node.asText();

        return isBlank(value)
                ? null
                : value;
    }

    private String getNestedText(
            JsonNode root,
            String objectName,
            String fieldName
    ) {

        if (root == null
                || objectName == null
                || fieldName == null) {
            return null;
        }

        JsonNode objectNode = root.get(objectName);

        if (objectNode == null
                || objectNode.isNull()
                || !objectNode.isObject()) {
            return null;
        }

        return getText(
                objectNode,
                fieldName
        );
    }

    private UUID parseUuid(
            String value,
            String fieldName
    ) {

        if (isBlank(value)) {
            return null;
        }

        try {
            return UUID.fromString(
                    value.trim()
            );

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    fieldName
                            + "는 올바른 UUID 형식이어야 합니다.",
                    e
            );
        }
    }

    private String resolveFileName(
            String fileName
    ) {

        if (isBlank(fileName)) {
            return "attachment";
        }

        return fileName.trim();
    }

    private String resolveContentType(
            String contentType
    ) {

        if (isBlank(contentType)) {
            return "application/octet-stream";
        }

        return contentType.trim();
    }

    private String firstNonBlank(
            String... values
    ) {

        if (values == null) {
            return null;
        }

        for (String value : values) {

            if (!isBlank(value)) {
                return value.trim();
            }
        }

        return null;
    }

    private boolean isBlank(
            String value
    ) {
        return value == null
                || value.isBlank();
    }
}