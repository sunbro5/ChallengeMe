package org.jan.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SessionAuthFilter sessionAuthFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5174"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(sessionAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints — always public
                .requestMatchers(HttpMethod.GET,  "/api/auth/captcha").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register",
                        "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                // Public read endpoints — home page map, game catalogue, player profiles, leaderboard
                .requestMatchers(HttpMethod.GET, "/api/events/public").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/games", "/api/games/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/players/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/leaderboard").permitAll()
                // WebSocket upgrade — auth happens inside the STOMP CONNECT frame
                .requestMatchers("/api/ws/**").permitAll()
                // Admin-only section
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Everything else requires a valid session
                .anyRequest().authenticated()
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            // Return JSON errors instead of redirecting to a login page
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(401);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"error\":\"Not logged in\"}");
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(403);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"error\":\"Access denied\"}");
                })
            );

        return http.build();
    }
}
