package com.avs.conversia.auth_service.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final String jwtSecret;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(String jwtSecret, UserDetailsService userDetailsService) {
        System.out.println("JWT SECRET (AuthService): " + jwtSecret);
        this.jwtSecret = jwtSecret;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = parseToken(request);

        if (token != null && validateToken(token)) {
            String username = getUsernameFromToken(token);

            // 🔐 Carrega o usuário real do banco
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }


    private String parseToken(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

   private boolean validateToken(String token) {
    try {
        Jwts.parser()
            .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
            .parseClaimsJws(token);
        return true;
    } catch (JwtException | IllegalArgumentException e) {
        e.printStackTrace();
        return false;
    }
}

private String getUsernameFromToken(String token) {
    Claims claims = Jwts.parser()
            .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
            .parseClaimsJws(token)
            .getBody();
    return claims.getSubject();
}

}


