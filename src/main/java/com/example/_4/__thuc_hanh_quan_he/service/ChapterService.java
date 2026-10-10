package com.example._4.__thuc_hanh_quan_he.service;

import com.example._4.__thuc_hanh_quan_he.Repository.ChapterRepository;
import com.example._4.__thuc_hanh_quan_he.entity.Chapter;
import com.example._4.__thuc_hanh_quan_he.entity.ChapterContent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChapterService {

    ChapterRepository chapterRepository;

    public Chapter create(Chapter chapter) {
        return chapterRepository.save(chapter);
    }

    public List<Chapter> getChaptersByStory(int storyId) {
        return chapterRepository.findByStoryIdOrderByVolNumberAscChapterNumberAsc(storyId);
    }

    @Transactional
    public Chapter getById(int id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương với ID: " + id));
    }

    @Transactional
    public void update(int id, Chapter details) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương"));

        chapter.setTitle(details.getTitle());
        chapter.setChapterNumber(details.getChapterNumber());
        chapter.setSlug(details.getSlug());
        chapter.setVolNumber(details.getVolNumber());

        if (details.getChapterContent() != null) {
            if (chapter.getChapterContent() == null) {
                details.getChapterContent().setChapter(chapter);
                chapter.setChapterContent(details.getChapterContent());
            } else {
                chapter.getChapterContent().setContentText(details.getChapterContent().getContentText());
            }
        }

        save(chapter);
    }

    public void delete(int id) {
        chapterRepository.deleteById(id);
    }

    public long countTotalChapters() {
        return chapterRepository.count();
    }

    @Transactional
    public void save(Chapter chapter) {

        if (chapter.getVolNumber() == null) chapter.setVolNumber(0);

        float numberToCheck = (float) (Math.round(chapter.getChapterNumber() * 10.0) / 10.0);
        chapter.setChapterNumber(numberToCheck);

        List<Chapter> existingChapters = chapterRepository.findByStoryIdAndVolNumberAndChapterNumber(
                chapter.getStory().getId(), chapter.getVolNumber(), numberToCheck);

        if (chapter.getId() != null) {
            existingChapters.removeIf(c -> c.getId().equals(chapter.getId()));
        }

        if (!existingChapters.isEmpty()) {
            System.out.println("Phát hiện trùng với các ID: " + existingChapters.stream().map(Chapter::getId).toList());

            throw new RuntimeException("Chương số " + numberToCheck + " đã tồn tại trong Tập " + chapter.getVolNumber() + "!");
        }

        if (chapter.getId() == null) chapter.setCreatedAt(LocalDateTime.now());

        if (chapter.getChapterContent() != null) {
            String text = chapter.getChapterContent().getContentText();
            int count = (text == null || text.trim().isEmpty()) ? 0 :
                    text.replaceAll("<[^>]*>", " ").trim().split("\\s+").length;
            chapter.setWordCount(count);
            chapter.getChapterContent().setWordCount(count);
            chapter.getChapterContent().setChapter(chapter);
        }
        if (chapter.getSlug() == null || chapter.getSlug().trim().isEmpty()) {
            chapter.setSlug("chuong-" + chapter.getChapterNumber());
        }

        chapterRepository.save(chapter);
    }
}