package com.example._4.__thuc_hanh_quan_he.service;

import com.example._4.__thuc_hanh_quan_he.Repository.*;
import com.example._4.__thuc_hanh_quan_he.entity.Story;
import com.example._4.__thuc_hanh_quan_he.entity.StoryStats;
import com.example._4.__thuc_hanh_quan_he.entity.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StoryService {

    StoryRepository storyRepository;
    StoryStatsRepository storyStatsRepository;
    ReadHistoryRepository readHistoryRepository;
    CommentRepository commentRepository;
    TagRepository tagRepository;

    @Transactional
    public Story create(Story story) {
        Story savedStory = storyRepository.save(story);

        StoryStats initialStats = new StoryStats();
        initialStats.setStory(savedStory);
        initialStats.setViewCount(0);
        storyStatsRepository.save(initialStats);

        return savedStory;
    }

    @Transactional
    public void update(Integer id, Story updatedStory) {
        storyRepository.saveAndFlush(updatedStory);
    }

    @Transactional
    public void delete(Integer id) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy truyện ID: " + id));

        // 1. Xóa hết các thứ liên quan trực tiếp đến Story ID trước
        readHistoryRepository.deleteByStoryId(id);
        commentRepository.deleteByStoryId(id);

        // 2. Dọn dẹp Tag (bảng trung gian)
        story.getTags().clear();

        // 3. Xử lý Stats
        if (story.getStoryStats() != null) {
            // Nếu có repository của stats thì xóa ở đây, hoặc để cascade lo
            story.setStoryStats(null);
        }

        storyRepository.saveAndFlush(story);

        // 4. Cuối cùng mới xóa truyện
        storyRepository.delete(story);
    }

    public Story getById(Integer id) {
        return storyRepository.findById(id)
                .orElse(null);
    }

    public Story getStoryByIdSafe(Integer id) {
        return storyRepository.findById(id).orElse(null);
    }

    public Story getBySlug(String slug) {
        Story story = storyRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy truyện với slug: " + slug));

        storyStatsRepository.incrementViewCount(story.getId());

        return story;
    }

    public List<Story> getAll() {
        return storyRepository.findAll();
    }

    public List<Story> getNewestStories() {
        return storyRepository.findTop10ByApprovalStatusOrderByUpdatedAtDesc(Story.StoryStatus.PUBLISHED);
    }

    public List<Story> getHotStories() {
        return storyRepository.findTop5ByIsHotTrueAndApprovalStatusOrderByUpdatedAtDesc(Story.StoryStatus.PUBLISHED);
    }

    public List<Story> getCompletedStories() {
        return storyRepository.findByStatusAndApprovalStatus(Story.Status.COMPLETED, Story.StoryStatus.PUBLISHED);
    }


    public List<Story> searchStories(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return storyRepository.findAll();
        }
        return storyRepository.findByTitleOrAuthorNameContaining(keyword.trim());
    }


    public List<Story> getAllLatest() {
        return storyRepository.findAllByApprovalStatusOrderByCreatedAtDesc(Story.StoryStatus.PUBLISHED);
    }

    public long count() {
        return storyRepository.count();
    }

    public List<Story> getTopFeaturedStories() {
        // Hàm này trong repo cũ tên là findTop5ByOrderByViewsDesc
        return storyRepository.findTop5ByApprovalStatusOrderByViewsDesc(Story.StoryStatus.PUBLISHED);
    }

    public List<Story> getStoriesByStatus(Story.Status status) {
        // Thêm tham số thứ 2 là Story.StoryStatus.PUBLISHED vào đây
        return storyRepository.findByStatusAndApprovalStatus(status, Story.StoryStatus.PUBLISHED);
    }



    public List<Story> advancedSearch(String keyword, String authorName, Integer tagId, String statusStr) {
        Story.Status status = null;

        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = Story.Status.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                status = null;
            }
        }

        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String searchAuthor = (authorName != null && !authorName.trim().isEmpty()) ? authorName.trim() : null;

        // Đảm bảo thứ tự truyền vào: searchKeyword -> searchAuthor -> tagId -> status -> PUBLISHED
        return storyRepository.advancedSearch(searchKeyword, searchAuthor, tagId, status, Story.StoryStatus.PUBLISHED);
    }

    @Transactional
    public void saveStoryWithTags(Story story, List<Integer> tagIds) {
        // 1. Gán Tag
        if (tagIds != null && !tagIds.isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(tagIds));
            story.setTags(tags);
        }

        // 2. Lưu Story
        Story savedStory = storyRepository.save(story);

        // 3. Khởi tạo Stats nếu truyện này là truyện mới hoàn toàn (chưa có ID)
        if (savedStory.getStoryStats() == null) {
            StoryStats initialStats = new StoryStats();
            initialStats.setStory(savedStory);
            initialStats.setViewCount(0);
            storyStatsRepository.save(initialStats);
        }
    }

    public List<Story> getStoriesByUploader(int uploaderId) {
        // Giả sử Minh đã có StoryRepository
        return storyRepository.findByUploaderIdOrderByCreatedAtDesc(uploaderId);
    }

    public Story getStoryById(Integer id) {
        return storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy truyện với ID: " + id));
    }

    public void deleteStory(Integer id) {
        // Có thể thêm logic xóa file ảnh ở đây trước khi xóa trong DB
        storyRepository.deleteById(id);
    }

    // 1. Lấy danh sách truyện có trạng thái kiểm duyệt là CHỜ DUYỆT (PENDING)
    public List<Story> getPendingStories() {
        // Hãy đảm bảo trong StoryRepository bạn đã đổi sang tìm theo field approvalStatus:
        // findByApprovalStatus(Story.StoryStatus approvalStatus)
        return storyRepository.findByApprovalStatus(Story.StoryStatus.PENDING);
    }

    // 2. Xử lý phê duyệt hoặc từ chối truyện từ phía Admin
    @Transactional
    public boolean reviewStory(Integer storyId, String action) { // Đồng bộ kiểu dữ liệu Integer id
        Optional<Story> storyOptional = storyRepository.findById(storyId);

        if (storyOptional.isPresent()) {
            Story story = storyOptional.get();

            if ("approve".equalsIgnoreCase(action)) {
                story.setApprovalStatus(Story.StoryStatus.PUBLISHED); // Duyệt thành công -> Xuất bản
            } else if ("reject".equalsIgnoreCase(action)) {
                story.setApprovalStatus(Story.StoryStatus.REJECTED);  // Từ chối truyện
            }

            storyRepository.save(story);
            return true;
        }
        return false;
    }
}