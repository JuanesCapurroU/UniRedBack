package com.unired.infrastructure.security;

import com.unired.config.JwtConfig;
import com.unired.domain.model.Sesion;
import com.unired.domain.repository.SesionRepository;
import com.unired.util.constants.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final SesionRepository sesionRepository;
    private final JwtConfig jwtConfig;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String correlationId = resolveCorrelationId(request);
        MDC.put(SecurityConstants.CORRELATION_ID_KEY, correlationId);
        request.setAttribute(SecurityConstants.CORRELATION_ID_KEY, correlationId);

        try {
            String authHeader = request.getHeader("Authorization");
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                processTokenAuthentication(token, request);
            }

            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(SecurityConstants.CORRELATION_ID_KEY);
        }
    }

    private void processTokenAuthentication(String token, HttpServletRequest request) {
        try {
            String username = jwtUtil.extractUsername(token);

            if (!StringUtils.hasText(username) || SecurityContextHolder.getContext().getAuthentication() != null) {
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!jwtUtil.isTokenValid(token, userDetails)) {
                return;
            }

            Optional<Sesion> optionalSesion = sesionRepository.findByTokenJwtAndActivoTrue(token);
            if (optionalSesion.isEmpty() || !optionalSesion.get().isValido()) {
                return;
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            Sesion sesion = optionalSesion.get();
            sesion.setFechaExpiracion(LocalDateTime.now().plus(Duration.ofMillis(jwtConfig.getExpirationMs())));
            sesionRepository.save(sesion);
        } catch (Exception ex) {
            log.debug("Token validation failed. correlationId={}", MDC.get(SecurityConstants.CORRELATION_ID_KEY));
        }
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String incoming = request.getHeader("X-Correlation-Id");
        return StringUtils.hasText(incoming) ? incoming : UUID.randomUUID().toString();
    }
}
