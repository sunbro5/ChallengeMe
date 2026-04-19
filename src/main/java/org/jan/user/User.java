package org.jan.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_username", columnList = "username", unique = true),
    @Index(name = "idx_user_email",    columnList = "email",    unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String email;
    private String role    = "USER";
    private boolean banned = false;
    private int birthYear;

    private int wins     = 0;
    private int losses   = 0;
    private int draws    = 0;
    private int disputes = 0;
    private int rating   = 1000;

    @Column(length = 160)
    private String bio;
    private String favoriteGameKey;

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email    = email;
        this.role     = "USER";
    }

    public User(String username, String password, String email, String role) {
        this.username = username;
        this.password = password;
        this.email    = email;
        this.role     = role;
    }
}
