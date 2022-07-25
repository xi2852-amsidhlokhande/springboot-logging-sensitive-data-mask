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

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper contentCachingRequestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper contentCachingResponseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        String requestBody = getStringValue(contentCachingRequestWrapper.getContentAsByteArray(), contentCachingRequestWrapper.getCharacterEncoding());
        log.info("REQUEST_METHOD={}, REQUEST_URL={}, REQUEST_PAYLOAD={}", request.getMethod(), request.getRequestURL().toString(), requestBody);

        filterChain.doFilter(contentCachingRequestWrapper, contentCachingResponseWrapper);

        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;
        String responseBody = getStringValue(contentCachingResponseWrapper.getContentAsByteArray(), contentCachingResponseWrapper.getCharacterEncoding());
        log.info("RESPONSE_CODE={}, TIME_TAKEN={}, RESPONSE_PAYLOAD={}", response.getStatus(), timeTaken, responseBody);
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
