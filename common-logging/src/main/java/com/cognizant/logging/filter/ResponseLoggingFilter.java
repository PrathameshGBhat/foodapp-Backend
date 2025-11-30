package com.cognizant.logging.filter;

import com.cognizant.logging.util.LoggingUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Component
public class ResponseLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ResponseLoggingFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LoggingUtil loggingUtil;

    public ResponseLoggingFilter(LoggingUtil loggingUtil) {
        this.loggingUtil = loggingUtil;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        CustomHttpResponseWrapper responseWrapper = new CustomHttpResponseWrapper((HttpServletResponse) response);

        long start = System.currentTimeMillis();
        chain.doFilter(request, responseWrapper);
        long duration = System.currentTimeMillis() - start;

        String traceId = loggingUtil.getTraceId();
        String spanId = loggingUtil.getSpanId();

        Map<String, Object> respLog = new HashMap<>();
        respLog.put("traceId", traceId);
        respLog.put("spanId", spanId);
        respLog.put("status", responseWrapper.getStatus());
        respLog.put("durationMs", duration);
        respLog.put("body", responseWrapper.getCaptureAsString());

        log.info("RESPONSE : {}", objectMapper.writeValueAsString(respLog));

        response.getWriter().write(responseWrapper.getCaptureAsString());
    }

    private static class CustomHttpResponseWrapper extends HttpServletResponseWrapper {

        private final CharArrayWriter charWriter = new CharArrayWriter();
        private final PrintWriter writer = new PrintWriter(charWriter);

        public CustomHttpResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public PrintWriter getWriter() {
            return writer;
        }

        public String getCaptureAsString() {
            return charWriter.toString();
        }
    }
}
