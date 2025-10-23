package com.ecommerce.ecommerce.security;

import com.ecommerce.ecommerce.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that reads Authorization header, validates JWT,
 * checks blacklist, and sets authentication in the security context.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private com.ecommerce.ecommerce.security.JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthService authService; // Added for blacklist checking

    // Inject AuthService into the filter (via constructor)
    public JwtAuthFilter(JwtUtil jwtUtil,
                         CustomUserDetailsService userDetailsService,
                         AuthService authService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = req.getHeader("Authorization");
        String token = null;

        // Step 1: Extract Bearer token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // Step 2: Validate and check blacklist before authentication
        if (token != null && jwtUtil.validate(token)) {

            // ✅ Check if token is blacklisted (user logged out)
            if (authService.isTokenBlacklisted(token)) {
                // If blacklisted, reject request immediately
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"success\": false, \"message\": \"Token has been invalidated. Please login again.\"}");
                return;
            }

            // Step 3: Extract email (or username) claim from token
            String email = jwtUtil.parseClaims(token).getBody().get("email", String.class);

            // Step 4: Only set authentication if user not already authenticated
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Step 5: Create authentication token
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                // Step 6: Set authentication in the SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Step 7: Continue filter chain
        chain.doFilter(req, res);
    }

}