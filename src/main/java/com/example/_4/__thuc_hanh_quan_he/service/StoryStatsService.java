package com.example._4.__thuc_hanh_quan_he.service;

import com.example._4.__thuc_hanh_quan_he.Repository.StoryStatsRepository;
import com.example._4.__thuc_hanh_quan_he.entity.StoryStats;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StoryStatsService {

    StoryStatsRepository storyStatsRepository;


    public StoryStats getStatsByStoryId(Integer storyId) {
        return storyStatsRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thống kê cho truyện ID: " + storyId));
    }


    @Transactional
    public void incrementView(Integer storyId) {
        storyStatsRepository.incrementViewCount(storyId);
    }

    @Transactional
    public void incrementViewWithSession(Integer storyId, jakarta.servlet.http.HttpSession session) {

        String sessionKey = "VIEWED_STORY_" + storyId;

        if (session.getAttribute(sessionKey) == null) {

            storyStatsRepository.incrementViewCount(storyId);

            session.setAttribute(sessionKey, true);

            System.out.println("Đã tăng 1 view cho truyện ID: " + storyId);
        } else {
            System.out.println("Người dùng vừa xem truyện này, không tăng view.");
        }
    }

    public long getTotalViews() {
        Long total = storyStatsRepository.sumAllViewCounts();
        return total != null ? total : 0;
    }
}