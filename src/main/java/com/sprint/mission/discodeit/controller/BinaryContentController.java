package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequiredArgsConstructor
@Controller
@ResponseBody
@RequestMapping("/binary")
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<UUID> create(@RequestBody BinaryContentCreateRequest request) {

        BinaryContent saved = binaryContentService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved.getId());
    }

    @RequestMapping(
            value = "/{id}",
            method = RequestMethod.GET)
    public ResponseEntity<byte[]> download(@PathVariable UUID id) {

        BinaryContent file = binaryContentService.find(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(file.getFileName())
                        .build()
        );

        return new ResponseEntity<>(
                file.getBytes(),
                headers,
                HttpStatus.OK
        );
    }

    @RequestMapping(value = "/zip", method = RequestMethod.GET)
    public ResponseEntity<byte[]> downloadZip(@RequestParam List<UUID> ids) throws IOException {

        List<BinaryContent> files = binaryContentService.findAllByIdIn(ids);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        for (BinaryContent file : files) {

            ZipEntry entry = new ZipEntry(file.getFileName());
            zos.putNextEntry(entry);
            zos.write(file.getBytes());
            zos.closeEntry();
        }

        zos.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("binary.zip")
                        .build()
        );

        // 음...
        return new ResponseEntity<>(
                baos.toByteArray(),
                headers,
                HttpStatus.OK
        );
    }
}