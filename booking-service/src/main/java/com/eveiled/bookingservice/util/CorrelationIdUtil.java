package com.eveiled.bookingservice.util;

import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.UUID;

public class CorrelationIdUtil {
    
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String MDC_CORRELATION_ID_KEY = "correlationId";
    public static final String MDC_BOOKING_ID_KEY = "bookingId";

    public static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    public static void setCorrelationId(String correlationId) {
        if (StringUtils.hasText(correlationId)) {
            MDC.put(MDC_CORRELATION_ID_KEY, correlationId);
        }
    }

    public static String getCorrelationId() {
        return MDC.get(MDC_CORRELATION_ID_KEY);
    }

    public static void setBookingId(Long bookingId) {
        if (bookingId != null) {
            MDC.put(MDC_BOOKING_ID_KEY, bookingId.toString());
        }
    }

    public static String getBookingId() {
        return MDC.get(MDC_BOOKING_ID_KEY);
    }

    public static void clear() {
        MDC.remove(MDC_CORRELATION_ID_KEY);
        MDC.remove(MDC_BOOKING_ID_KEY);
    }

    public static void clearCorrelationId() {
        MDC.remove(MDC_CORRELATION_ID_KEY);
    }

    public static void clearBookingId() {
        MDC.remove(MDC_BOOKING_ID_KEY);
    }
}
