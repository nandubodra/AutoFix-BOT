package com.nandubodra.sitesentinel.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LoginBlockFilter extends OncePerRequestFilter {

    private final LoginAttemptService loginAttemptService;

    public LoginBlockFilter(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().equals("/login")) {
            String email = request.getParameter("username");
            if (email != null && loginAttemptService.isBlocked(email.toLowerCase())) {
                response.sendRedirect("/login?blocked");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
