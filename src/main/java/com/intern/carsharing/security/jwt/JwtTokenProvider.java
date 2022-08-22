package com.intern.carsharing.security.jwt;

import com.intern.carsharing.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@Setter
@RequiredArgsConstructor
public class JwtTokenProvider {
    private static final int TOKEN_START_INDEX = 7;
    private static final String ROLES_CLAIMS = "roles";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_START = "Bearer ";
    private final UserDetailsService userDetailsService;
    @Value("${jwt.token.secret}")
    private String secret;
    @Value("${jwt.token.expired}")
    private long expirationPeriod;

    @PostConstruct
    protected void init() {
        secret = Base64.getEncoder().encodeToString(secret.getBytes());
    }

    public String createToken(String email, Set<Role> roles) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put(ROLES_CLAIMS, getRoleNames(new ArrayList<>(roles)));
        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationPeriod);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        String email = getUserName(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(
                userDetails, email, userDetails.getAuthorities());
    }

    public String getUserName(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
    }

    public String resolveToken(HttpServletRequest request) {
        String token = request.getHeader(AUTHORIZATION_HEADER);
        if (token != null && token.startsWith(BEARER_START)) {
            return token.substring(TOKEN_START_INDEX);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            //throw new JwtTokenInvalidException("Jwt token expired or invalid");
            return false;
        }
    }

    private List<String> getRoleNames(List<Role> roles) {
        return roles.stream()
                .map(role -> role.getRoleName().toString())
                .collect(Collectors.toList());
    }
}
