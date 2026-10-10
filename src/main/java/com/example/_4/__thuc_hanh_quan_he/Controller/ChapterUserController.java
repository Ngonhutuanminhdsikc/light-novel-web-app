package com.example._4.__thuc_hanh_quan_he.Controller;

import com.example._4.__thuc_hanh_quan_he.entity.Chapter;
import com.example._4.__thuc_hanh_quan_he.entity.ChapterContent;
import com.example._4.__thuc_hanh_quan_he.entity.Story;
import com.example._4.__thuc_hanh_quan_he.service.ChapterService;
import com.example._4.__thuc_hanh_quan_he.service.StoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user/chapters")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChapterUserController {

    ChapterService chapterService;
    StoryService storyService;

    @ModelAttribute("chapter")
    public Chapter getChapter(@PathVariable(required = false) Integer storyId) {
        Chapter chapter = new Chapter();
        chapter.setChapterContent(new ChapterContent()); // Khởi tạo object con để tránh null
        return chapter;
    }

    private void checkOwnership(Integer storyId, Principal principal) {
        var story = storyService.getStoryByIdSafe(storyId);

        if (story == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Truyện không tồn tại");
        }

        if (story.getUploader() != null && !story.getUploader().getUserName().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập");
        }
    }

    // 1. Xem danh sách chương
    @GetMapping("/{storyId}")
    public String listChapters(@PathVariable("storyId") Integer storyId, Model model, Principal principal) {
        Story story = storyService.getStoryByIdSafe(storyId); // Dùng hàm Safe

        if (story == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy truyện");
        }

        // 3. Kiểm tra quyền sở hữu (403)
        if (story.getUploader() != null && !story.getUploader().getUserName().equals(principal.getName())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập"
            );
        }

        // 4. Lấy dữ liệu chương
        List<Chapter> chapters = chapterService.getChaptersByStory(storyId);
        model.addAttribute("story", story);
        model.addAttribute("chapters", chapters);

        return "user/chapters/index";
    }

    // 2. Hiển thị form THÊM chương mới
    @GetMapping("/add/{storyId}")
    public String showAddForm(@PathVariable("storyId") Integer storyId, Model model, Principal principal) {
        checkOwnership(storyId, principal);
        Story story = storyService.getStoryById(storyId);
        model.addAttribute("story", story);

        Chapter chapter = new Chapter();
        chapter.setChapterContent(new ChapterContent());
        model.addAttribute("chapter", chapter);

        return "user/chapters/add";
    }

    // 3. Xử lý LƯU chương mới
    @PostMapping("/save")
    public String handleSaveChapter(
            @Valid @ModelAttribute("chapter") Chapter chapter,
            BindingResult result,
            @RequestParam("storyId") Integer storyId,
            Model model,
            Principal principal
    ) {
        // Ép buộc kiểm tra nội dung rỗng ngay cả khi object con không tồn tại
        if (chapter.getChapterContent() == null ||
                chapter.getChapterContent().getContentText() == null ||
                chapter.getChapterContent().getContentText().isBlank()) {

            result.rejectValue("chapterContent.contentText", "error.chapter", "Nội dung không được để trống");
        }

        if (result.hasErrors()) {
            model.addAttribute("story", storyService.getStoryById(storyId));
            return "user/chapters/add";
        }
        try {
            checkOwnership(storyId, principal);
            chapter.setStory(storyService.getStoryById(storyId));

            chapterService.save(chapter);
            return "redirect:/user/chapters/" + storyId;
        } catch (RuntimeException e) {

            result.rejectValue("chapterNumber", "error.chapter", e.getMessage());
            model.addAttribute("story", storyService.getStoryById(storyId));
            return "user/chapters/add";
        }
    }

    // 4. Lưu và sửa
    @GetMapping({"/edit/{chapterId}", "/write/{chapterId}"})
    public String showEditForm(@PathVariable("chapterId") Integer chapterId, Model model, Principal principal) {
        Chapter chapter = chapterService.getById(chapterId);

        if (chapter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chương không tồn tại");
        }

        checkOwnership(chapter.getStory().getId(), principal);

        if (chapter.getChapterContent() == null) {
            chapter.setChapterContent(new ChapterContent());
        }

        model.addAttribute("chapter", chapter);
        model.addAttribute("story", chapter.getStory());
        return "user/chapters/edit";
    }

    // 5. Xử lý CẬP NHẬT chương
    @PostMapping("/update/{chapterId}")
    public String handleUpdateChapter(
            @PathVariable("chapterId") Integer chapterId,
            @Valid @ModelAttribute("chapter") Chapter chapterDetails,
            BindingResult result,
            Model model,
            Principal principal
    ) {
        if (result.hasErrors()) {
            model.addAttribute("story", chapterService.getById(chapterId).getStory());
            return "user/chapters/edit";
        }

        try {
            Chapter existingChapter = chapterService.getById(chapterId);
            checkOwnership(existingChapter.getStory().getId(), principal);

            chapterService.update(chapterId, chapterDetails);
            return "redirect:/user/chapters/" + existingChapter.getStory().getId();
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/user/chapters/edit/" + chapterId + "?error=true";
        }
    }

    // 6. XỬ LÝ XÓA CHƯƠNG
    @GetMapping("/delete/{chapterId}")
    public String deleteChapter(@PathVariable("chapterId") Integer chapterId,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        Chapter chapter = chapterService.getById(chapterId);

        if (chapter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chương không tồn tại");
        }

        Integer storyId = chapter.getStory().getId();
        checkOwnership(storyId, principal);

        chapterService.delete(chapterId);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa chương thành công!");
        return "redirect:/user/chapters/" + storyId;
    }
}