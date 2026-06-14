package com.soundkh.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RequestLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        long start = System.currentTimeMillis();
        String method = request.getMethod();
        String uri    = request.getRequestURI();
        String query  = request.getQueryString();
        String user   = request.getHeader("Authorization") != null ? "(auth)" : "(anon)";

        try {
            chain.doFilter(req, res);
        } finally {
            long ms     = System.currentTimeMillis() - start;
            int  status = response.getStatus();
            String level = status >= 500 ? "ERROR" : status >= 400 ? "WARN" : "INFO";

            String msg = "[{}] {} {}{} → {} ({}ms)";
            String fullUri = query != null ? uri + "?" + query : uri;

            if ("ERROR".equals(level))      log.error(msg, user, method, fullUri, "", status, ms);
            else if ("WARN".equals(level))  log.warn (msg, user, method, fullUri, "", status, ms);
            else                            log.info (msg, user, method, fullUri, "", status, ms);
        }
    }
}
