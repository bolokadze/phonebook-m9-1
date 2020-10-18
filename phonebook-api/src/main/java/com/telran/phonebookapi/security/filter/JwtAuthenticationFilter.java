package com.telran.phonebookapi.security.filter;

import com.telran.phonebookapi.security.model.JWToken;
import com.telran.phonebookapi.security.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.apache.catalina.filters.ExpiresFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailService;
    private final JwtService jwtService;
    private final String tokenHeader;

    public JwtAuthenticationFilter(UserDetailsService userDetailService, JwtService jwtService, String tokenHeader) {
        this.userDetailService = userDetailService;
        this.jwtService = jwtService;
        this.tokenHeader = tokenHeader;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String tokenString = request.getHeader(tokenHeader);
        if (tokenString == null) {
            filterChain.doFilter(request, response);
            return;
        }
        JWToken token;
        try {
            token = jwtService.parseToken(tokenString);
        } catch (ExpiredJwtException | SignatureException e) {
            filterChain.doFilter(request, response);
            return;
        }
           String username = token.getUsername();
        UserDetails userDetails = userDetailService.loadUserByUsername(username);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }
}

