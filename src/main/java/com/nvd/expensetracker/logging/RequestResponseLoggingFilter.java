package com.nvd.expensetracker.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class RequestResponseLoggingFilter implements Filter {

    private static final Logger reqLogger = LoggerFactory.getLogger("REQUEST_RESPONSE_LOGGER");

    // List of URIs that do not need logging
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/swagger", "/swagger-ui", "/swagger-ui.html", "/v3/api-docs", "/favicon.ico"
    );

    private static final int MAX_BODY_LENGTH = 1000;

    private boolean isExcluded(String uri) {
        return EXCLUDED_PATHS.stream().anyMatch(uri::startsWith);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        String requestUri = httpReq.getRequestURI();

        // Ignore log if route is in exclusion list
        if (isExcluded(requestUri)) {
            chain.doFilter(request, response);
            return;
        }

        // Wrap to read body
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpReq);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper((HttpServletResponse) response);

        reqLogger.info("Incoming request: {} {}", wrappedRequest.getMethod(), requestUri);

        chain.doFilter(wrappedRequest, wrappedResponse);

        // Log body request
        String requestBody = new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8);
        if (!requestBody.isBlank()) {
            reqLogger.info("Request body: {}", shorten(requestBody));
        }

        // Log body response
        String responseBody = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
        reqLogger.info("Outgoing response: status={}, body={}", wrappedResponse.getStatus(), shorten(responseBody));

        wrappedResponse.copyBodyToResponse();
    }

    private String shorten(String body) {
        if (body.length() <= MAX_BODY_LENGTH) return body;
        return body.substring(0, MAX_BODY_LENGTH) + "...(truncated)";
    }
}
