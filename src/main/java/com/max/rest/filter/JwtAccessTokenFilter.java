package com.max.rest.filter;

import com.max.rest.dto.JwtAuthentication;
import com.max.rest.service.JwtProviderService;
import com.max.rest.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAccessTokenFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String USERS_ENDPOINT = "/api/users";
    private static final List<String> PERMITTED_ENDPOINTS = Arrays.asList(
            "/api/auth/",
            "/swagger-ui/",
            "/v3/api-docs/"
    );

    private final JwtProviderService jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ipAddress = request.getRemoteAddr();
        String requestURI = request.getRequestURI();

        boolean isPermittedEndpoint = isPermittedEndpoint(requestURI);

        // Check PERMITTED_ENDPOINTS to skip token validation
        if (isPermittedEndpoint) {
            log.info("Received request from IP: {}, to {} - allowed.", ipAddress, requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // Check USERS_ENDPOINT while POST method to allow registration
        if (isUsersEndpoint(requestURI)) {
            if (isPostRequest(request)) {
                log.info("Received request from IP: {}, to {} - allowed.", ipAddress, requestURI);
                filterChain.doFilter(request, response);
                return;
            }
        }


        final String token = getTokenFromRequest(request);
        if (token != null && jwtProvider.isValidAccessToken(token)) {
            final Claims claims = jwtProvider.getAccessClaims(token);
            final JwtAuthentication jwtInfoToken = JwtUtils.generate(claims);
            jwtInfoToken.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(jwtInfoToken);
            log.info("Received request from IP: {}, to {} - allowed.", ipAddress, requestURI);
        } else
            log.info("Received request from IP: {}, to {} - not allowed.", ipAddress, requestURI);
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        final String bearer = request.getHeader(AUTHORIZATION);
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring("Bearer ".length());
        }
        return null;
    }

    private boolean isPermittedEndpoint(String requestURI) {
        return PERMITTED_ENDPOINTS.stream().anyMatch(requestURI::startsWith);
    }

    private boolean isUsersEndpoint(String requestURI) {
        return requestURI.equals(USERS_ENDPOINT);
    }

    private boolean isPostRequest(HttpServletRequest request) {
        return HttpMethod.POST.matches(request.getMethod());
    }
}

