package com.example._4.__thuc_hanh_quan_he.Controller;

import com.example._4.__thuc_hanh_quan_he.entity.Chapter;
import com.example._4.__thuc_hanh_quan_he.entity.ChapterContent;
import com.example._4.__thuc_hanh_quan_he.service.ChapterService;
import com.example._4.__thuc_hanh_quan_he.service.StoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/chaptersController")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChapterController {

    final ChapterService chapterService;
    final StoryService storyService;


    private void checkOwnership(int storyId, Principal principal) {
        var story = storyService.getById(storyId);
        if (story != null && story.getUploader() != null &&
                !story.getUploader().getUserName().equals(principal.getName())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        }
    }

    // 1. Xem danh sách chương
    @GetMapping("/story/{storyId}")
    public String listChapters(@PathVariable int storyId, Model model) {
        model.addAttribute("story", storyService.getById(storyId));
        model.addAttribute("chapters", chapterService.getChaptersByStory(storyId));
        return "admin/chapters/index";
    }

    // 2. Form thêm chương mới
    @GetMapping("/add/{storyId}")
    public String showAddForm(@PathVariable int storyId, Model model, Principal principal) {
        checkOwnership(storyId, principal);

        Chapter chapter = new Chapter();

        chapter.setChapterContent(new ChapterContent());

        model.addAttribute("chapter", new Chapter());
        model.addAttribute("story", storyService.getById(storyId));
        return "admin/chapters/add-chapter";
    }

    // 3. Xử lý lưu chương mới
    @PostMapping("/save")
    public String handleAdd(@Valid @ModelAttribute("chapter") Chapter chapter, BindingResult result,
                            @RequestParam int storyId, Principal principal, Model model) { // Thay RedirectAttributes bằng Model

        if (result.hasErrors()) {
            // Trả về thẳng view, không redirect
            model.addAttribute("story", storyService.getById(storyId));
            return "admin/chapters/add-chapter";
        }

        try {
            checkOwnership(storyId, principal);
            chapter.setStory(storyService.getById(storyId));
            chapterService.save(chapter);
            // Ở đây mới dùng redirect vì đã thành công
            return "redirect:/chaptersController/story/" + storyId;
        } catch (Exception e) {
            return "redirect:/chaptersController/add/" + storyId;
        }
    }

    // 4. Form sửa THÔNG TIN (Nút bút chì vàng)
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model, Principal principal) {
        Chapter chapter = chapterService.getById(id);
        if (chapter == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
        }
        checkOwnership(chapter.getStory().getId(), principal);
        model.addAttribute("chapter", chapter);
        return "admin/chapters/edit-chapter";
    }

    // 5. Xử lý cập nhật THÔNG TIN
    @PostMapping("/edit/{id}")
    public String handleUpdate(@PathVariable int id, @Valid @ModelAttribute("chapter") Chapter chapter,
                               BindingResult result, @RequestParam int storyId, Principal principal, RedirectAttributes ra) {

        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            ra.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/chaptersController/edit/" + id;
        }

        try {
            checkOwnership(storyId, principal);
            chapterService.update(id, chapter);
            ra.addFlashAttribute("successMessage", "Cập nhật thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/chaptersController/edit/" + id;
        }

        return "redirect:/chaptersController/story/" + storyId;
    }

    // 6. Xóa chương
    @Transactional
    @GetMapping("/delete/{id}")
    public String handleDelete(@PathVariable int id, Principal principal, RedirectAttributes ra) {
        try {
            Chapter chapter = chapterService.getById(id);
            if (chapter == null) {
                ra.addFlashAttribute("errorMessage", "Chương không tồn tại!");
                return "redirect:/storiesController";
            }

            checkOwnership(chapter.getStory().getId(), principal);

            int storyId = chapter.getStory().getId();

            chapterService.delete(id);

            ra.addFlashAttribute("successMessage", "Đã xóa chương thành công!");
            return "redirect:/chaptersController/story/" + storyId;

        } catch (org.springframework.web.server.ResponseStatusException e) {
            ra.addFlashAttribute("errorMessage", "Bạn không có quyền xóa chương này!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể xóa chương này vì còn dữ liệu liên quan!");
            e.printStackTrace(); // Vẫn in ra console để bạn debug
        }
        return "redirect:/storiesController";
    }
}