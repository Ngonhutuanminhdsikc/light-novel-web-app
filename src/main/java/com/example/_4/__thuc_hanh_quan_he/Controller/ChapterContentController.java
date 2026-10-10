package com.example._4.__thuc_hanh_quan_he.Controller;

import com.example._4.__thuc_hanh_quan_he.entity.Chapter;
import com.example._4.__thuc_hanh_quan_he.entity.ChapterContent;
import com.example._4.__thuc_hanh_quan_he.entity.UserAccount;
import com.example._4.__thuc_hanh_quan_he.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChapterContentController {

    ChapterContentService contentService;
    ChapterService chapterService;
    StoryStatsService storyStatsService;
    ReadHistoryService readHistoryService;
    UserAccountService userAccountService;

    //  Xem và Chỉnh sửa nội dung
    @GetMapping("/edit/{chapterId}")
    public String showEditContent(@PathVariable int chapterId, Model model,Principal principal) {

        Chapter chk = chapterService.getById(chapterId);
        if (chk != null && chk.getStory().getUploader() != null &&
                !chk.getStory().getUploader().getUserName().equals(principal.getName())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        }

        ChapterContent content = contentService.findById(chapterId);
        if (content == null) {
            content = new ChapterContent();
            content.setChapterId(chapterId);
        }
        model.addAttribute("content", content);
        model.addAttribute("chapter", chapterService.getById(chapterId));
        return "admin/chapter-content/edit-content";
    }

    //  Lưu nội dung chương
    @PostMapping("/save")
    public String handleSaveContent(@RequestParam("chapterId") int chapterId,
                                    @RequestParam("contentText") String contentText,
                                    Principal principal,
                                    RedirectAttributes ra,
                                    Model model) {

        Chapter chk = chapterService.getById(chapterId);
        if (chk != null && chk.getStory().getUploader() != null &&
                !chk.getStory().getUploader().getUserName().equals(principal.getName())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        }

        if (contentText == null || contentText.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Nội dung chương không được để trống!");
            model.addAttribute("chapter", chk);


            ChapterContent errorContent = new ChapterContent();
            errorContent.setChapterId(chapterId);
            errorContent.setContentText(contentText);
            model.addAttribute("content", errorContent);

            return "admin/chapter-content/edit-content";
        }

        contentService.saveContent(chapterId, contentText);

        ra.addFlashAttribute("successMessage", "Cập nhật nội dung thành công!");
        return "redirect:/chaptersController/story/" + chk.getStory().getId();
    }


    @GetMapping("/reading/chapter/{id}")
    public String reading(@PathVariable int id, Model model, HttpSession session, Principal principal) {
        Chapter currentChapter = chapterService.getById(id);
        if (currentChapter == null) return "redirect:/";

        int storyId = currentChapter.getStory().getId();


        if (principal != null) {
            try {
                UserAccount user = userAccountService.findByUserName(principal.getName());


                readHistoryService.saveHistory(user.getId(), storyId, id);

            } catch (Exception e) {

                e.printStackTrace();
            }
        }


        Set<Integer> viewedStories = (Set<Integer>) session.getAttribute("viewedStories");
        if (viewedStories == null) {
            viewedStories = new HashSet<>();
        }

        if (!viewedStories.contains(storyId)) {
            storyStatsService.incrementView(storyId);
            viewedStories.add(storyId);
            session.setAttribute("viewedStories", viewedStories);
        }


        List<Chapter> chapters = chapterService.getChaptersByStory(storyId);

        Integer prevId = null;
        Integer nextId = null;

        for (int i = 0; i < chapters.size(); i++) {
            if (chapters.get(i).getId() == id) {

                if (i > 0) prevId = chapters.get(i - 1).getId();
                if (i < chapters.size() - 1) nextId = chapters.get(i + 1).getId();
                break;
            }
        }


        ChapterContent chapterContent = contentService.findById(id);


        model.addAttribute("chapter", currentChapter);
        model.addAttribute("story", currentChapter.getStory());
        model.addAttribute("chapters", chapters);
        model.addAttribute("prevId", prevId);
        model.addAttribute("nextId", nextId);
        model.addAttribute("content", chapterContent);

        return "user/reading";
    }
}