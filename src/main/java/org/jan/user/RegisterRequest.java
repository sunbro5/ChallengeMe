package org.jan.user;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String captchaId;
    private String captchaAnswer;
    private int birthYear;
}
