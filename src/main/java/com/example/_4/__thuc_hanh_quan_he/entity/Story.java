package com.example._4.__thuc_hanh_quan_he.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "STORY")
@Builder
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    Integer id;

    @NotBlank(message = "Tên bộ truyện không được để trống!")
    @Size(min = 2, max = 200, message = "Tên bộ truyện phải từ 2 đến 200 ký tự!")
    @Column(name = "TITLE", nullable = false, length = 200)
    String title;

    @NotBlank(message = "Slug của truyện không được để trống!")
    @Column(name = "SLUG", nullable = false, unique = true)
    String slug;

    @ManyToOne
    @JoinColumn(name = "AUTHOR_ID", referencedColumnName = "ID")
    Author author;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    String description;

    @Column(name = "THUMBNAIL_URL")
    String thumbnailUrl;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    Status status;

    @Column(name = "APPROVAL_STATUS")
    @Enumerated(EnumType.STRING)
    StoryStatus approvalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UPLOADER_ID")
    UserAccount uploader;

    @Column(name = "IS_HOT", columnDefinition = "BOOLEAN DEFAULT FALSE")
    boolean isHot;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    LocalDateTime updatedAt;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "STORY_TAG",
            joinColumns = @JoinColumn(name = "STORY_ID"),
            inverseJoinColumns = @JoinColumn(name = "TAG_ID")
    )
    Set<Tag> tags = new HashSet<>();

    @OneToOne(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    StoryStats storyStats;

    @Builder.Default
    @OneToMany(mappedBy = "story", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    @ToString.Exclude
    List<Chapter> chapters = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Follow> followers = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    List<Comment> comments = new ArrayList<>();

    public enum Status {
        ONGOING, COMPLETED, DROPPED
    }

    public enum StoryStatus {
        PENDING, PUBLISHED, REJECTED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = Status.ONGOING;
        if (approvalStatus == null) approvalStatus = StoryStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getLastChapterName() {
        if (this.chapters == null || this.chapters.isEmpty()) return null;
        return this.chapters.get(0).getTitle();
    }
}