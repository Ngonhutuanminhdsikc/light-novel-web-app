package com.example._4.__thuc_hanh_quan_he.service;

import com.example._4.__thuc_hanh_quan_he.Repository.ChapterContentRepository;
import com.example._4.__thuc_hanh_quan_he.Repository.ChapterRepository;
import com.example._4.__thuc_hanh_quan_he.entity.Chapter;
import com.example._4.__thuc_hanh_quan_he.entity.ChapterContent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChapterContentService {

    ChapterContentRepository chapterContentRepository;
    ChapterRepository chapterRepository;

    @Transactional
    public ChapterContent saveContent(int chapterId, String text) {

        Chapter parentChapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter không tồn tại"));

        ChapterContent content = chapterContentRepository.findById(chapterId)
                .orElseGet(() -> {
                    ChapterContent newContent = new ChapterContent();
                    newContent.setChapter(parentChapter);
                    return newContent;
                });

        int calculatedWordCount = 0;
        if (text != null && !text.trim().isEmpty()) {

            String cleanText = text.replaceAll("<[^>]*>", " ").trim();
            if (!cleanText.isEmpty()) {
                calculatedWordCount = cleanText.split("\\s+").length;
            }
        }

        content.setContentText(text);
        content.setWordCount(calculatedWordCount);

        parentChapter.setWordCount(calculatedWordCount);
        chapterRepository.save(parentChapter);

        return chapterContentRepository.save(content);
    }

    public ChapterContent findById(int chapterId) {
        return chapterContentRepository.findById(chapterId).orElse(null);
    }

    public ChapterContent getContentByChapterId(int chapterId) {
        return chapterContentRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Nội dung chương này hiện đang trống"));
    }

    @Transactional
    public void deleteContent(int chapterId) {
        chapterContentRepository.deleteById(chapterId);
    }
}