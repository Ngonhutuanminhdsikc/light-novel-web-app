package com.example._4.__thuc_hanh_quan_he.Repository;

import com.example._4.__thuc_hanh_quan_he.entity.ReadHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadHistoryRepository extends JpaRepository<ReadHistory, ReadHistory.ReadHistoryId> {
    // Tìm lịch sử đọc của 1 User
    List<ReadHistory> findById_UserIdOrderByReadAtDesc(int userId);

    // Tìm xem user đã từng đọc truyện
    Optional<ReadHistory> findByUserIdAndStoryId(int userId, int storyId);

    void deleteByStoryId(Integer storyId);
}