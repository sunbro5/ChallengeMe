package org.jan.user.dto;

import lombok.Value;

@Value
public class LoginResponse {
    String username;
    String role;
}
