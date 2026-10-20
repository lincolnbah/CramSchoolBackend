package com.school.common.aspect.filter;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {
    public static final String TRACE_ID_KEY = "traceId";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_ATTRIBUTE = TraceIdFilter.class.getName() + ".traceId";

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Map<String, String> previousContext = MDC.getCopyOfContextMap();
        String traceId = (String) request.getAttribute(TRACE_ID_ATTRIBUTE);
        if (traceId == null) {
            traceId = IdUtil.simpleUUID();
            request.setAttribute(TRACE_ID_ATTRIBUTE, traceId);
        }
        long startNanos = System.nanoTime();
        try {
            MDC.put(TRACE_ID_KEY, traceId);
            response.setHeader(TRACE_ID_HEADER, traceId);
            log.info("開始處理請求 method={} path={}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
        } finally {
            try {
                log.info("結束請求處理 method={} path={} status={} elapsedMs={} asyncStarted={}",
                        request.getMethod(), request.getRequestURI(), response.getStatus(),
                        (System.nanoTime() - startNanos) / 1_000_000, request.isAsyncStarted());
            } finally {
                if (previousContext == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previousContext);
                }
            }
        }
    }
}
