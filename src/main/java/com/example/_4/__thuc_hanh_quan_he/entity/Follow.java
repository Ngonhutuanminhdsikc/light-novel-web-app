package com.example._4.__thuc_hanh_quan_he.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "follows")
@Builder
public class Follow {

    @EmbeddedId
    FollowId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "USER_ID")
    UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("storyId")
    @JoinColumn(name = "STORY_ID")
    Story story;

    @Embeddable
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FollowId implements Serializable {
        @Column(name = "USER_ID")
        Integer userId;

        @Column(name = "STORY_ID")
        Integer storyId;
    }
}