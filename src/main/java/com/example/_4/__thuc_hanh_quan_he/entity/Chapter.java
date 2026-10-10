package com.example._4.__thuc_hanh_quan_he.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "CHAPTER")
@Builder
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    Integer id;

    @ManyToOne
    @JoinColumn(name = "STORY_ID", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    Story story;

    @Min(value = 0, message = "Số chương không được nhỏ hơn 0")
    @NotNull(message = "Số chương không được để trống")
    @Column(name = "CHAPTER_NUMBER", nullable = false)
    Float chapterNumber;

    @NotBlank(message = "Tiêu đề chương không được để trống!")
    @Size(max = 255, message = "Tiêu đề chương không được vượt quá 255 ký tự!")
    @Column(name = "TITLE")
    String title;


    @Column(name = "SLUG", nullable = false)
    String slug;

    @Valid
    @OneToOne(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    ChapterContent chapterContent;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "WORD_COUNT")
    Integer wordCount;

    @Min(value = 1, message = "Tập truyện phải bắt đầu từ 1")
    @NotNull(message = "Tập không được để trống")
    @Column(name = "VOL_NUMBER")
    Integer volNumber;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}