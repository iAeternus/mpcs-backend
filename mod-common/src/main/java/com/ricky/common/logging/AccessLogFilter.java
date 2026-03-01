package com.ricky.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AccessLogFilter extends OncePerRequestFilter {
    private static final Logger ACCESS_LOG = LoggerFactory.getLogger("ACCESS_LOG");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            String uri = request.getRequestURI();
            String method = request.getMethod();
            int status = response.getStatus();
            ACCESS_LOG.info("{} {} {} {}", method, uri, status, elapsed);
        }
    }
}