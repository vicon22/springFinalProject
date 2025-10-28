package com.eveiled.bookingservice.config;

import com.eveiled.bookingservice.util.CorrelationIdUtil;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .filter((request, next) -> {
                    String correlationId = CorrelationIdUtil.getCorrelationId();
                    if (StringUtils.hasText(correlationId)) {
                        return next.exchange(
                            ClientRequest.from(request)
                                .header(CorrelationIdUtil.CORRELATION_ID_HEADER, correlationId)
                                .build()
                        );
                    }
                    return next.exchange(request);
                });
    }
}
