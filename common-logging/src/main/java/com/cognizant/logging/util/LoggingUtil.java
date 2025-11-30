package com.cognizant.logging.util;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoggingUtil {

    private final Tracer tracer;

    public String getTraceId() {
        return tracer.currentSpan() != null ? tracer.currentSpan().context().traceId() : "N/A";
    }

    public String getSpanId() {
        return tracer.currentSpan() != null ? tracer.currentSpan().context().spanId() : "N/A";
    }
}
