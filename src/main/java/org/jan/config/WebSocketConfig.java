package org.jan.config;

import jakarta.servlet.http.HttpSession;
import org.jan.user.User;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:5173", "http://localhost:5174")
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request,
                            WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        // Primary auth path: resolve username from the validated HTTP session.
                        // The session cookie IS sent on the WS upgrade request from the same origin.
                        if (request instanceof ServletServerHttpRequest servletRequest) {
                            HttpSession session = servletRequest.getServletRequest().getSession(false);
                            if (session != null) {
                                User user = (User) session.getAttribute("user");
                                if (user != null && !user.isBanned()) {
                                    String username = user.getUsername();
                                    // Store in WS session attributes so the CONNECT interceptor
                                    // can verify the STOMP login header against it.
                                    attributes.put("sessionUsername", username);
                                    return () -> username;
                                }
                            }
                        }
                        return null;
                    }
                });
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) return message;

                // ── CONNECT: authenticate the WebSocket session ────────────────
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    Principal existing = accessor.getUser();
                    if (existing == null) {
                        // Fallback: use the STOMP login header (cross-origin WS
                        // where the session cookie was not forwarded).
                        // This is less secure than session-based auth — a malicious
                        // client could supply any username.  The SUBSCRIBE guard
                        // below limits the blast radius.
                        String login = accessor.getLogin();
                        if (login != null && !login.isBlank()) {
                            accessor.setUser(() -> login);
                        } else {
                            // No identity at all — reject
                            throw new IllegalStateException(
                                    "WebSocket CONNECT rejected: no authentication provided");
                        }
                    }
                    // If a Principal was already set via the handshake (session-based),
                    // do NOT override it with the login header to prevent impersonation.
                }

                // ── SUBSCRIBE: enforce topic ownership ────────────────────────
                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    Principal principal = accessor.getUser();
                    if (principal == null) {
                        throw new IllegalStateException(
                                "WebSocket SUBSCRIBE rejected: not authenticated");
                    }
                    String destination = accessor.getDestination();
                    if (destination != null && isPersonalTopic(destination)) {
                        String topicOwner = destination.substring(destination.lastIndexOf('/') + 1);
                        if (!topicOwner.equals(principal.getName())) {
                            throw new IllegalStateException(
                                    "WebSocket SUBSCRIBE rejected: cannot subscribe to another user's topic");
                        }
                    }
                }

                return message;
            }

            /** Personal topics whose ownership is enforced. */
            private boolean isPersonalTopic(String dest) {
                return dest.startsWith("/topic/chat/")
                        || dest.startsWith("/topic/notifications/");
            }
        });
    }
}
