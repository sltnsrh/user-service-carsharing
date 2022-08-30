package com.intern.carsharing.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "confirmation_tokens")
public class ConfirmationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, name = "token")
    private String token;
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;
    @Column(nullable = false, name = "expired_at")
    private LocalDateTime expiredAt;
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id")
    private User user;
}
