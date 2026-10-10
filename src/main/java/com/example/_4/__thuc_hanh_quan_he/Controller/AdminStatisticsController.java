package com.example._4.__thuc_hanh_quan_he.Controller;

import com.example._4.__thuc_hanh_quan_he.Repository.StoryRepository;
import com.example._4.__thuc_hanh_quan_he.Repository.StoryStatsRepository;
import com.example._4.__thuc_hanh_quan_he.entity.Story;
import com.example._4.__thuc_hanh_quan_he.service.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminStatisticsController {

    StoryService storyService;
    UserAccountService userService;
    AuthorService authorService;
    TagService tagService;
    ChapterService chapterService;
    StoryStatsService storyStatsService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public String showStatistics(Model model) {
        model.addAttribute("activePage", "statistics");

        long totalStories = storyService.count();
        long dropped = storyService.getStoriesByStatus(Story.Status.DROPPED).size();
        long completed = storyService.getCompletedStories().size();
        model.addAttribute("totalStories", totalStories);
        model.addAttribute("totalUsers", userService.count());
        model.addAttribute("totalAuthors", authorService.count());

        model.addAttribute("totalChapters", chapterService.countTotalChapters());
        model.addAttribute("totalViews", storyStatsService.getTotalViews());


        model.addAttribute("topStories", storyService.getTopFeaturedStories());

        model.addAttribute("completedStories", completed);
        model.addAttribute("ongoingStories", totalStories - completed - dropped);
        model.addAttribute("droppedStories", dropped);

        return "admin/statistics";
    }
}