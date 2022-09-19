package com.intern.carsharing.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.carsharing.exception.ApiExceptionObject;
import java.io.IOException;
import java.time.ZonedDateTime;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
public class JwtTokenFilter extends OncePerRequestFilter {
    private static final String JSON_TYPE = "application/json";
    private static final String JWT_INVALID_MESSAGE = "Jwt token not valid or expired";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void doFilterInternal(
            @NotNull HttpServletRequest servletRequest,
            @NotNull HttpServletResponse servletResponse,
            @NotNull FilterChain filterChain
    ) throws IOException, ServletException {
        String token = jwtTokenProvider
                .resolveToken(servletRequest.getHeader(AUTHORIZATION_HEADER));
        if (token != null) {
            if (jwtTokenProvider.validateToken(token)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                setResponseException(servletResponse);
                return;
            }
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (IllegalArgumentException e) {
            setResponseException(servletResponse);
        }
    }

    private void setResponseException(HttpServletResponse servletResponse) throws IOException {
        servletResponse.setContentType(JSON_TYPE);
        ApiExceptionObject message = new ApiExceptionObject(
                JWT_INVALID_MESSAGE,
                HttpStatus.UNAUTHORIZED,
                ZonedDateTime.now().toString()
        );
        servletResponse.getWriter().write(objectMapper.writeValueAsString(message));
        servletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
    }
}
