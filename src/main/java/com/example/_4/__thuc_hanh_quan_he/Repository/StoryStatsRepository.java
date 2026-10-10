package com.example._4.__thuc_hanh_quan_he.Repository;

import com.example._4.__thuc_hanh_quan_he.entity.StoryStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface StoryStatsRepository extends JpaRepository<StoryStats, Integer> {

    // Hàm lấy thông số theo Story ID
    StoryStats findByStoryId(int storyId);

    @Modifying
    @Transactional
    @Query("UPDATE StoryStats s SET s.viewCount = s.viewCount + 1 WHERE s.storyId = :storyId")
    void incrementViewCount(@Param("storyId") int storyId);

    @Query("SELECT SUM(s.viewCount) FROM StoryStats s")
    Long sumAllViewCounts();


}
