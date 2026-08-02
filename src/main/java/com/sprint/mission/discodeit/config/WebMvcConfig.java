package com.sprint.mission.discodeit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    /**
     * 컨트롤러의 전, 후에 요청을 훅처럼 낚아채서, 헤더에 추가적인 정보를 조작한다
     *
     * WebMvcConfigure 로 인터셉터를 구현하고, addInterceptor 를 활용해서
     * Handdler 를 등록,
     * handler 가 직접 요청을 처리하는 로직이다.
     *
     * 순서는
     * tomcat -> prehandle -> controller -> posthandle -> response 생성 -> afterCompletion
     */

    private final MDCLoggingInterceptor mdcLoggingInterceptor;

    public WebMvcConfig(MDCLoggingInterceptor mdcLoggingInterceptor) {
        this.mdcLoggingInterceptor = mdcLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mdcLoggingInterceptor);
    }
}
