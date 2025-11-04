package com.kb.healthcare.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@Table(name = "users",
        indexes = {
                @Index(columnList = "recordKey", unique = true),
                @Index(columnList = "email", unique = true)
        })
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String recordKey;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    public User(String recordKey, String name, String nickname, String email, String passwordHash) {
        this.recordKey = recordKey;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.passwordHash = passwordHash;
    }
}