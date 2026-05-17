package com.example.store.config;

import org.slf4j.MDC;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.util.UUID;

@Configuration
public class RequestLoggingConfiguration {

    @Bean
    public FilterRegistrationBean<CommonsRequestLoggingFilter> requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter() {
            @Override
            protected void beforeRequest(jakarta.servlet.http.HttpServletRequest request, String message) {
                String requestId = request.getHeader("X-Request-Id");
                if (requestId == null || requestId.isBlank()) requestId = UUID.randomUUID().toString();
                MDC.put("requestId", requestId);
            }

            @Override
            protected void afterRequest(jakarta.servlet.http.HttpServletRequest request, String message) {
                MDC.remove("requestId");
            }
        };
        filter.setIncludeClientInfo(true);
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(false); // avoid logging payload by default
        filter.setIncludeHeaders(false);
        filter.setMaxPayloadLength(1024);

        FilterRegistrationBean<CommonsRequestLoggingFilter> registrationBean = new FilterRegistrationBean<>((CommonsRequestLoggingFilter) filter);
        registrationBean.setOrder(100);
        return registrationBean;
    }
}

