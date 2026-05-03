package com.akaslan.orderservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    @Value("${integration.cart.internal-api-key:local-dev-internal-key}")
    private String internalApiKey;

    @Override
    public void apply(RequestTemplate template) {
        template.header(INTERNAL_API_KEY_HEADER, internalApiKey);
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String token = attributes.getRequest().getHeader(AUTHORIZATION_HEADER);
            if (token != null) {
                template.header(AUTHORIZATION_HEADER, token);
            }
        }
    }
}
