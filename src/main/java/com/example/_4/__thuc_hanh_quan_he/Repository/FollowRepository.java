package com.example._4.__thuc_hanh_quan_he.Repository;

import com.example._4.__thuc_hanh_quan_he.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Follow.FollowId> {

    // 1. Đếm số lượng người theo dõi để hiển thị con số cạnh trái tim
    long countByIdStoryId(Integer storyId);

    // 2. Tìm danh sách truyện đã theo dõi của một User để hiện trong "Tủ truyện"
    List<Follow> findByIdUserId(Integer userId);

    // 3. Kiểm tra xem User hiện tại đã theo dõi truyện
    boolean existsById(Follow.FollowId id);
}