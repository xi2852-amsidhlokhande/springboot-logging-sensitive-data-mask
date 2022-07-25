package com.amsidh.mvc.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper contentCachingRequestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper contentCachingResponseWrapper = new ContentCachingResponseWrapper(response);
        long startTime = System.currentTimeMillis();
        filterChain.doFilter(contentCachingRequestWrapper, contentCachingResponseWrapper);
        long endTime = System.currentTimeMillis();

        String requestBody = getStringValue(contentCachingRequestWrapper.getContentAsByteArray(), contentCachingRequestWrapper.getCharacterEncoding());
        String responseBody = getStringValue(contentCachingResponseWrapper.getContentAsByteArray(), contentCachingResponseWrapper.getCharacterEncoding());

        log.info("Finished processing- REQUEST_METHOD={}, REQUEST_URL={}, REQUEST_PAYLOAD={}, RESPONSE_CODE={}, RESPONSE_PAYLOAD={} TIME_TAKEN={} millisecond", request.getMethod(), request.getRequestURL().toString(), requestBody, response.getStatus(), responseBody, (endTime - startTime), kv("BackendSystemName", "logging-sensitive-data"));

        contentCachingResponseWrapper.copyBodyToResponse();

    }

    private String getStringValue(byte[] byteData, String characterEncoding) {
        try {
            return new String(byteData, 0, byteData.length, characterEncoding).replaceAll("[\r\n]+", "");
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            log.error("Unsupported encoding type {} exception occurred", characterEncoding, unsupportedEncodingException);
        }
        return "";
    }
}
