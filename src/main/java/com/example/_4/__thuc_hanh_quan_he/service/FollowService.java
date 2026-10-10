package com.example._4.__thuc_hanh_quan_he.service;

import com.example._4.__thuc_hanh_quan_he.Repository.FollowRepository;
import com.example._4.__thuc_hanh_quan_he.Repository.StoryRepository;
import com.example._4.__thuc_hanh_quan_he.Repository.UserAccountRepository;
import com.example._4.__thuc_hanh_quan_he.entity.Follow;
import com.example._4.__thuc_hanh_quan_he.entity.Story;
import com.example._4.__thuc_hanh_quan_he.entity.UserAccount;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FollowService {

    FollowRepository followRepository;
    StoryRepository storyRepository;
    UserAccountRepository userAccountRepository;


    @Transactional
    public String toggleFollow(Integer userId, Integer storyId) {
        Follow.FollowId followId = new Follow.FollowId(userId, storyId);

        if (followRepository.existsById(followId)) {
            followRepository.deleteById(followId);
            return "UNFOLLOWED";
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy truyện"));

        Follow follow = Follow.builder()
                .id(followId)
                .user(user)
                .story(story)
                .build();

        followRepository.save(follow);
        return "FOLLOWED";
    }


    public long countFollowers(Integer storyId) {
        return followRepository.countByIdStoryId(storyId);
    }

    public boolean isFollowing(Integer userId, Integer storyId) {
        return followRepository.existsById(new Follow.FollowId(userId, storyId));
    }

    public List<Follow> getFollowsByUser(Integer userId) {
        return followRepository.findByIdUserId(userId);
    }
}