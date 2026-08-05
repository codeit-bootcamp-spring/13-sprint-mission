package com.sprint.mission.discodeit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DiscodeitInfoContributor implements InfoContributor {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    @Value("${discodeit.storage.type}")
    private String storageType;

    @Value("${discodeit.storage.local.root-path}")
    private String storagePath;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @Value("${spring.servlet.multipart.max-request-size}")
    private String maxRequestSize;

    @Override
    public void contribute(Info.Builder builder) {

        builder.withDetail("datasourceUrl", Map.of(
                "url", datasourceUrl,
                "driver", driverClassName
        ));
        builder.withDetail("jpa", Map.of(
                "ddl-auto", ddlAuto
        ));
        builder.withDetail("storage", Map.of(
                "type", storageType,
                "path", storagePath
        ));
        builder.withDetail("multipart", Map.of(
                "max-file-size", maxFileSize,
                "max-request-size", maxRequestSize
        ));
    }
}
