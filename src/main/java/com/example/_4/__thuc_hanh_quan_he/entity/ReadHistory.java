package com.example._4.__thuc_hanh_quan_he.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "READ_HISTORY")
@Builder
public class ReadHistory {

    @EmbeddedId
    ReadHistoryId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "USER_ID")
    UserAccount user;

    @ManyToOne
    @MapsId("storyId")
    @JoinColumn(name = "STORY_ID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    Story story;

    @ManyToOne
    @JoinColumn(name = "CHAPTER_ID", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    Chapter lastReadChapter;

    @Column(name = "READ_AT", nullable = false)
    LocalDateTime readAt;

    @Embeddable
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class ReadHistoryId implements Serializable {
        @Column(name = "USER_ID")
        int userId;

        @Column(name = "STORY_ID")
        int storyId;
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        readAt = LocalDateTime.now();
    }
}