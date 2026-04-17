package org.jan.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jan.user.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads the User stored in the HTTP session on each request and populates
 * the Spring Security context.  This bridges the custom session-based auth
 * with Spring Security so that route rules in SecurityConfig are enforced.
 *
 * No DB hit here — ban checking is done by BanCheckInterceptor which runs
 * after the dispatcher servlet dispatches to the handler.
 */
@Component
public class SessionAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null && !user.isBanned()) {
                var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());
                var auth = new UsernamePasswordAuthenticationToken(
                        user.getUsername(), null, List.of(authority));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }
}
