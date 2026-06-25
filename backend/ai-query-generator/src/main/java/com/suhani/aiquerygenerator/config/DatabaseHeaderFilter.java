package com.suhani.aiquerygenerator.config;

import com.suhani.aiquerygenerator.dto.ConnectionRequest;
import com.suhani.aiquerygenerator.util.DatabaseContext;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class DatabaseHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            HttpServletRequest http = (HttpServletRequest) request;
            String path = http.getRequestURI();
            if (path.startsWith("/api/")) {
                String url = http.getHeader("X-DB-Url");
                String username = http.getHeader("X-DB-Username");
                String password = http.getHeader("X-DB-Password");
                if (url != null && username != null && password != null) {
                    DatabaseContext.setCurrent(
                            DatabaseContext.ConnectionRequest.from(url, username, password));
                } else {
                    DatabaseContext.clear();
                }
            }
        } catch (Exception e) {
            // ignore
        }
        try {
            chain.doFilter(request, response);
        } finally {
            DatabaseContext.clear();
        }
    }
}