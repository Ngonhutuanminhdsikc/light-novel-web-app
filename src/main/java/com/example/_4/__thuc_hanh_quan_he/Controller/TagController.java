package com.example._4.__thuc_hanh_quan_he.Controller;

import com.example._4.__thuc_hanh_quan_he.entity.Tag;
import com.example._4.__thuc_hanh_quan_he.service.TagService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tagsController")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TagController {
    final TagService tagService;

    // 1. Hiển thị danh sách tất cả các nhãn
    @GetMapping
    public String getAllTags(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
        model.addAttribute("activePage", "tags");

        List<Tag> tags;
        if (keyword != null && !keyword.isEmpty()) {
            tags = tagService.searchByName(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            tags = tagService.getAll();
        }

        model.addAttribute("tags", tags);
        return "admin/tags/tags-index";
    }

    // 2. Hiển thị form thêm nhãn mới
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("tag", new Tag());
        return "admin/tags/add-tag";
    }

    // 3. Xử lý lưu nhãn mới
    @PostMapping("/add")
    public String handleAdd(@Valid @ModelAttribute("tag") Tag tag,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "admin/tags/add-tag";
        }

        try {
            tagService.create(tag);
            // THÊM THÔNG BÁO THÀNH CÔNG VÀO ĐÂY
            redirectAttributes.addFlashAttribute("successMessage", "Thêm nhãn mới thành công!");
            return "redirect:/tagsController";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Tên hoặc Slug đã tồn tại!");
            return "admin/tags/add-tag";
        }
    }

    // 4. Hiển thị form chỉnh sửa nhãn
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        model.addAttribute("tag", tagService.getById(id));
        return "admin/tags/edit-tag";
    }

    @PostMapping("/edit/{id}")
    public String handleUpdate(@PathVariable int id,
                               @Valid @ModelAttribute("tag") Tag tag,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "admin/tags/edit-tag";
        }

        try {
            tagService.update(id, tag);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thành công!");
            return "redirect:/tagsController";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên hoặc Slug này đã tồn tại, vui lòng đổi tên khác!");
            return "redirect:/tagsController/edit/" + id;
        }
    }

    // 6. Xóa nhãn theo ID (Có thông báo thành công hoặc lỗi khóa ngoại)
    @GetMapping("/delete/{id}")
    public String handleDelete(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            tagService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa Nhãn (Tag) thành công!");
        } catch (Exception e) {
            e.printStackTrace();

            if (e.getMessage() != null && e.getMessage().contains("foreign key")) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Không thể xóa Nhãn này vì đang có các bộ truyện sử dụng nó!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi thực hiện xóa nhãn này.");
            }
        }
        return "redirect:/tagsController";
    }

    @GetMapping("/genre/{slug}")
    public String getStoriesByTag(@PathVariable String slug, Model model) {
        Tag tag = tagService.getBySlug(slug);

        if (tag == null) {
            return "error/404";
        }

        model.addAttribute("tag", tag);
        model.addAttribute("stories", tag.getStories());

        return "user/tag_detail";
    }
}