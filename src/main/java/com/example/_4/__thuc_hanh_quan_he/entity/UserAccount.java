package com.example._4.__thuc_hanh_quan_he.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "USER_ACCOUNT")
@Builder
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    Integer id;

    @Size(min = 5, max = 50, message = "Username phải từ 6 đến 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Username chỉ được chứa chữ và số")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    String userName;

    @Size(min = 6, message = "Password phải có ít nhất 6 ký tự")
    @Column(name = "pass_word", nullable = false, length = 200)
    String passWord;

    @Column(name = "EMAIL", unique = true, length = 100)
    String email;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID")
    Role role;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    Status status;

    @Column(name = "AVATAR_URL")
    String avatarUrl;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "FULL_NAME", length = 100)
    String fullName;

    @Column(name = "BANNER_URL")
    String bannerUrl;

    @Column(name = "BIO", columnDefinition = "TEXT")
    String bio;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments;

    public enum Status {
        ACTIVE, BANNED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = Status.ACTIVE; // Mặc định là ACTIVE
    }
}