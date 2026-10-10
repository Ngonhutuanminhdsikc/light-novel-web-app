package com.example._4.__thuc_hanh_quan_he.Repository;

import com.example._4.__thuc_hanh_quan_he.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Integer> {

    Optional<Chapter> findById(int id);

    List<Chapter> findByStoryIdOrderByVolNumberAscChapterNumberAsc(Integer storyId);


    // Kiểm tra trùng khi thêm mới
    boolean existsByStoryIdAndVolNumberAndChapterNumber(int storyId, Integer volNumber, Float chapterNumber);

    // Kiểm tra trùng khi cập nhật
    boolean existsByStoryIdAndVolNumberAndChapterNumberAndIdNot(int storyId, Integer volNumber, Float chapterNumber, Integer id);


    List<Chapter> findByStoryIdAndVolNumberAndChapterNumber(int storyId, Integer volNumber, Float chapterNumber);
}

