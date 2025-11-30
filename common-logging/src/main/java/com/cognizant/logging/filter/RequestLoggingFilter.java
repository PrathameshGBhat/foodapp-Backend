package com.cognizant.logging.filter;

import com.cognizant.logging.util.LoggingUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

@Component
public class RequestLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LoggingUtil loggingUtil;

    public RequestLoggingFilter(LoggingUtil loggingUtil) {
        this.loggingUtil = loggingUtil;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        String traceId = loggingUtil.getTraceId();
        String spanId = loggingUtil.getSpanId();

        String body = getRequestBody(req);

        Map<String, Object> logMap = new HashMap<>();
        logMap.put("traceId", traceId);
        logMap.put("spanId", spanId);
        logMap.put("method", req.getMethod());
        logMap.put("uri", req.getRequestURI());
        logMap.put("query", req.getQueryString());
        logMap.put("headers", getHeaders(req));
        logMap.put("body", body);

        log.info("REQUEST : {}", objectMapper.writeValueAsString(logMap));

        chain.doFilter(request, response);
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            headers.put(key, request.getHeader(key));
        }
        return headers;
    }

    private String getRequestBody(HttpServletRequest request) {
        try {
            BufferedReader reader = request.getReader();
            return reader.lines().reduce("", (acc, line) -> acc + line);
        } catch (Exception e) {
            return "Unable to read body";
        }
    }
}
