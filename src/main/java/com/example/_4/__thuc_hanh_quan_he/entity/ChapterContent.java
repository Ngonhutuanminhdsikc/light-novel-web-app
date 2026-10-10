package com.example._4.__thuc_hanh_quan_he.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "CHAPTER_CONTENT")
@Builder
public class ChapterContent {

    @Id
    @Column(name = "CHAPTER_ID")
    Integer chapterId;


    @OneToOne
    @MapsId
    @JoinColumn(name = "CHAPTER_ID")
    Chapter chapter;

    @NotBlank(message = "Nội dung chương truyện không được để trống!")
    @Column(name = "CONTENT_TEXT", nullable = false, columnDefinition = "MEDIUMTEXT")
    String contentText;

    @Column(name = "WORD_COUNT")
    Integer wordCount;


}