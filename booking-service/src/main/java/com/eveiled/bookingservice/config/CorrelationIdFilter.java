package com.eveiled.bookingservice.config;

import com.eveiled.bookingservice.util.CorrelationIdUtil;
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
            
            if (!StringUtils.hasText(correlationId)) {
                correlationId = CorrelationIdUtil.generateCorrelationId();
                log.debug("Generated new correlation ID: {}", correlationId);
            } else {
                log.debug("Using existing correlation ID: {}", correlationId);
            }

            CorrelationIdUtil.setCorrelationId(correlationId);

            response.setHeader(CorrelationIdUtil.CORRELATION_ID_HEADER, correlationId);

            filterChain.doFilter(request, response);
            
        } finally {
            CorrelationIdUtil.clear();
        }
    }
}
