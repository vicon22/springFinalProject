package com.eveiled.hotelservice.util;

import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.UUID;

public class CorrelationIdUtil {
    
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String MDC_CORRELATION_ID_KEY = "correlationId";
    public static final String MDC_ROOM_ID_KEY = "roomId";

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

    public static void setRoomId(Long roomId) {
        if (roomId != null) {
            MDC.put(MDC_ROOM_ID_KEY, roomId.toString());
        }
    }

    public static String getRoomId() {
        return MDC.get(MDC_ROOM_ID_KEY);
    }

    public static void clear() {
        MDC.remove(MDC_CORRELATION_ID_KEY);
        MDC.remove(MDC_ROOM_ID_KEY);
    }

    public static void clearCorrelationId() {
        MDC.remove(MDC_CORRELATION_ID_KEY);
    }

    public static void clearRoomId() {
        MDC.remove(MDC_ROOM_ID_KEY);
    }
}
