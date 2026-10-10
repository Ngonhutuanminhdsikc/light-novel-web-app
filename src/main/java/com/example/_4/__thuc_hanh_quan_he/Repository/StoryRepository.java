package com.example._4.__thuc_hanh_quan_he.Repository;

import com.example._4.__thuc_hanh_quan_he.entity.Story;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, Integer> {

    @EntityGraph(attributePaths = {"tags", "author"})
    List<Story> findAll();

    // 1.Chỉ lấy 10 truyện mới cập nhật ĐÃ DUYỆT ngoài trang chủ
    @EntityGraph(attributePaths = {"tags"})
    List<Story> findTop10ByApprovalStatusOrderByUpdatedAtDesc(Story.StoryStatus approvalStatus);

    // 2.Chỉ lấy 5 truyện hot ĐÃ DUYỆT ngoài trang chủ
    @EntityGraph(attributePaths = {"tags"})
    List<Story> findTop5ByIsHotTrueAndApprovalStatusOrderByUpdatedAtDesc(Story.StoryStatus approvalStatus);

    // 3.Chỉ lấy truyện có trạng thái nội dung (ví dụ COMPLETED) và ĐÃ DUYỆT
    @EntityGraph(attributePaths = {"tags"})
    List<Story> findByStatusAndApprovalStatus(Story.Status status, Story.StoryStatus approvalStatus);


    @EntityGraph(attributePaths = {"author", "tags"})
    @Query("SELECT s FROM Story s LEFT JOIN s.author a WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Story> findByTitleOrAuthorNameContaining(@Param("keyword") String keyword);

    @EntityGraph(attributePaths = {"author", "tags"})
    Optional<Story> findBySlug(String slug);

    boolean existsBySlug(String slug);

    // 4.Lấy toàn bộ danh sách truyện mới nhất ĐÃ DUYỆT (cho trang /new)
    List<Story> findAllByApprovalStatusOrderByCreatedAtDesc(Story.StoryStatus approvalStatus);

    // 5. Lấy top truyện xem nhiều nhất và ĐÃ DUYỆT
    @EntityGraph(attributePaths = {"author", "storyStats"})
    @Query("SELECT s FROM Story s JOIN s.storyStats ss WHERE s.approvalStatus = :approvalStatus ORDER BY ss.viewCount DESC")
    List<Story> findTop5ByApprovalStatusOrderByViewsDesc(@Param("approvalStatus") Story.StoryStatus approvalStatus);

    // 6. Tìm kiếm nâng cao chỉ hiển thị truyện ĐÃ DUYỆT (Thêm điều kiện check approvalStatus)
    @Query("SELECT DISTINCT s FROM Story s " +
            "LEFT JOIN s.author a " + // Thêm JOIN sang bảng tác giả
            "LEFT JOIN s.tags t " +
            "WHERE s.approvalStatus = :approvalStatus " +
            "AND (:keyword IS NULL OR :keyword = '' OR LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:authorName IS NULL OR :authorName = '' OR LOWER(a.name) LIKE LOWER(CONCAT('%', :authorName, '%'))) " + // Thêm điều kiện lọc tác giả
            "AND (:tagId IS NULL OR t.id = :tagId) " +
            "AND (:status IS NULL OR s.status = :status)")
    List<Story> advancedSearch(@Param("keyword") String keyword,
                               @Param("authorName") String authorName, // Thêm nhận tham số tác giả ở đây
                               @Param("tagId") Integer tagId,
                               @Param("status") Story.Status status,
                               @Param("approvalStatus") Story.StoryStatus approvalStatus);

    // Lấy danh sách truyện đã được duyệt để hiện lên trang chủ
    @Query("SELECT s FROM Story s LEFT JOIN FETCH s.uploader WHERE s.approvalStatus = :approvalStatus")
    List<Story> findByApprovalStatus(@Param("approvalStatus") Story.StoryStatus approvalStatus);

    // Lấy danh sách truyện của một User cụ thể
    List<Story> findByUploaderId(Integer uploaderId);

    List<Story> findByUploaderIdOrderByCreatedAtDesc(Integer uploaderId);

    Optional<Story> findById(Integer id);

    List<Story> findByStatus(String status);


}