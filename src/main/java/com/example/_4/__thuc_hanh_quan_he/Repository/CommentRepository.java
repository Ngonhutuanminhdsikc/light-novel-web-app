package com.example._4.__thuc_hanh_quan_he.Repository;

import com.example._4.__thuc_hanh_quan_he.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByStoryIdAndParentCommentIsNullOrderByCreatedAtDesc(Integer storyId);

    // Thống kê bình luận mới nhất cho Dashboard
    List<Comment> findTop5ByOrderByIdDesc();

    void deleteByStoryId(Integer storyId);
}