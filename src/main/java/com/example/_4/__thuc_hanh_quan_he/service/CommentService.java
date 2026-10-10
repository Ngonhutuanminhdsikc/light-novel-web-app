package com.example._4.__thuc_hanh_quan_he.service;

import com.example._4.__thuc_hanh_quan_he.Repository.CommentRepository;
import com.example._4.__thuc_hanh_quan_he.entity.Comment;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService {
    CommentRepository commentRepository;

    public Comment getCommentById(int id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy comment với id: " + id));
    }

    public List<Comment> getCommentsByStory(int storyId) {
        return commentRepository.findByStoryIdAndParentCommentIsNullOrderByCreatedAtDesc(storyId);
    }

    public List<Comment> getLatestComments() {
        return commentRepository.findTop5ByOrderByIdDesc();
    }

    public void save(Comment comment) {
        commentRepository.save(comment);
    }

    // Trong CommentService.java
    @Transactional
    public void update(Comment comment) {

        if (commentRepository.existsById(comment.getId())) {
            commentRepository.save(comment);
        } else {
            throw new RuntimeException("Không tìm thấy comment để cập nhật!");
        }
    }

    @Transactional
    public void deleteComment(int id) {
        commentRepository.deleteById(id);
    }
}