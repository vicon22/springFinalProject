package com.eveiled.hotelservice.config;

import com.eveiled.hotelservice.util.CorrelationIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that extracts correlation ID from incoming requests
 * and sets it in MDC for logging and passes it to outgoing responses.
 */
@Component
@Order(1)
@Slf4j
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String correlationId = request.getHeader(CorrelationIdUtil.CORRELATION_ID_HEADER);
            
            if (StringUtils.hasText(correlationId)) {
                log.debug("Using correlation ID from request: {}", correlationId);
                CorrelationIdUtil.setCorrelationId(correlationId);

                response.setHeader(CorrelationIdUtil.CORRELATION_ID_HEADER, correlationId);
            } else {
                log.debug("No correlation ID found in request headers");
            }

            filterChain.doFilter(request, response);
            
        } finally {
            CorrelationIdUtil.clear();
        }
    }
}
