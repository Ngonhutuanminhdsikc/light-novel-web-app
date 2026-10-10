package com.example._4.__thuc_hanh_quan_he.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "STORY_STATS")
@Builder
public class StoryStats {

    @Id
    @Column(name = "STORY_ID")
    Integer storyId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "STORY_ID")
    Story story;

    @Column(name = "VIEW_COUNT", columnDefinition = "INT DEFAULT 0")
    int viewCount;
}