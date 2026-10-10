package com.example._4.__thuc_hanh_quan_he.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "COMMENT")
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    Integer id;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    UserAccount user;

    @ManyToOne
    @JoinColumn(name = "STORY_ID", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    Story story;

    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL)
    List<Comment> replies;

    @Column(name = "CONTENT", nullable = false, columnDefinition = "TEXT")
    String content;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}