package com.example.musicapi.config;

import com.example.musicapi.client.TokenHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.HttpRequest;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

@Configuration
public class RestTemplateConfig {

    private final TokenHolder tokenHolder;

    public RestTemplateConfig(TokenHolder tokenHolder) {
        this.tokenHolder = tokenHolder;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new AuthorizationInterceptor()));
        return restTemplate;
    }

    private class AuthorizationInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public org.springframework.http.client.ClientHttpResponse intercept(
                HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution
        ) throws IOException {
            if (tokenHolder.getAccessToken() != null) {
                request.getHeaders().set("Authorization", "Bearer " + tokenHolder.getAccessToken());
            }
            return execution.execute(request, body);
        }
    }
}
