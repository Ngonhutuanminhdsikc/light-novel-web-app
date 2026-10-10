package com.example._4.__thuc_hanh_quan_he.service;

import com.example._4.__thuc_hanh_quan_he.Repository.ReadHistoryRepository;
import com.example._4.__thuc_hanh_quan_he.Repository.StoryRepository;
import com.example._4.__thuc_hanh_quan_he.Repository.UserAccountRepository;
import com.example._4.__thuc_hanh_quan_he.entity.Chapter;
import com.example._4.__thuc_hanh_quan_he.entity.ReadHistory;
import com.example._4.__thuc_hanh_quan_he.entity.Story;
import com.example._4.__thuc_hanh_quan_he.entity.UserAccount;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ReadHistoryService {
    ReadHistoryRepository readHistoryRepository;
    UserAccountRepository userAccountRepository;
    StoryRepository storyRepository;

    @Transactional
    public void updateReadHistory(UserAccount user, Story story, Chapter chapter) {
        ReadHistory.ReadHistoryId historyId = new ReadHistory.ReadHistoryId(user.getId(), story.getId());

        ReadHistory history = readHistoryRepository.findById(historyId).orElse(null);

        if (history == null) {
            history = new ReadHistory();
            history.setId(historyId);
            history.setUser(user);
            history.setStory(story);
        }

        history.setLastReadChapter(chapter);
        readHistoryRepository.save(history);
    }

    public List<ReadHistory> getHistoryByUser(int userId) {
        return readHistoryRepository.findById_UserIdOrderByReadAtDesc(userId);
    }

    // Cập nhật lại phương thức saveHistory
    @Transactional
    public void saveHistory(int userId, int storyId, int chapterId) {
        ReadHistory.ReadHistoryId historyId = new ReadHistory.ReadHistoryId(userId, storyId);

        // 1. Tìm bản ghi cũ hoặc tạo mới
        ReadHistory history = readHistoryRepository.findById(historyId)
                .orElseGet(() -> {
                    ReadHistory newHistory = new ReadHistory();
                    newHistory.setId(historyId);
                    // Lấy entity thực tế từ repo để đảm bảo quan hệ JPA hợp lệ
                    newHistory.setUser(userAccountRepository.findById(userId).orElseThrow());
                    newHistory.setStory(storyRepository.findById(storyId).orElseThrow());
                    return newHistory;
                });

        // 2. Cập nhật thời gian và chapter
        history.setReadAt(LocalDateTime.now());

        // Gợi ý: Thay vì tạo mới Chapter, bạn nên dùng một ChapterService
        // hoặc lấy trực tiếp từ repository nếu cần validate sự tồn tại của chương
        Chapter chapter = new Chapter();
        chapter.setId(chapterId);
        history.setLastReadChapter(chapter);

        readHistoryRepository.save(history);
    }

    @Transactional
    public void deleteByStory(int userId, int storyId) {
        readHistoryRepository.deleteById(new ReadHistory.ReadHistoryId(userId, storyId));
    }
}
