package com.example._4.__thuc_hanh_quan_he.Controller;

import com.example._4.__thuc_hanh_quan_he.entity.Author;
import com.example._4.__thuc_hanh_quan_he.service.AuthorService;
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

import java.util.List;

@Controller
@RequestMapping("/authors")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorController {

    AuthorService authorService;

    // Hiển thị danh sách tác giả
    @GetMapping
    public String listAuthors(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
        List<Author> authors;

        if (keyword != null && !keyword.isEmpty()) {
            authors = authorService.searchByName(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            authors = authorService.getAll();
        }

        model.addAttribute("authors", authors);
        return "admin/author/authors-index";
    }

    // Hiển thị chi tiết một tác giả
    @GetMapping("/{id}")
    public String detailAuthor(@PathVariable int id, Model model) {
        model.addAttribute("author", authorService.getById(id));
        return "admin/author/detail";
    }

    // Hiển thị form add
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("author", new Author());
        return "admin/author/add-author";
    }

    // Xử lý lưu tác giả mới
    @PostMapping("/add")
    public String addAuthor(@Valid @ModelAttribute Author author,
                            BindingResult result,RedirectAttributes redirectAttributes,
                            @RequestParam(value = "confirm", required = false, defaultValue = "false") boolean confirm,
                            Model model) {
        try {

            if (result.hasErrors()) {
                return "admin/author/add-author";
            }

            List<Author> exactMatches = authorService.searchByName(author.getName().trim())
                    .stream()
                    .filter(a -> a.getName().trim().equalsIgnoreCase(author.getName().trim()))
                    .toList();

            if (!exactMatches.isEmpty() && !confirm) {
                model.addAttribute("warningMessage", "Hệ thống phát hiện đã có tác giả tên '" + author.getName() + "' rồi. Nếu đây là tác giả khác trùng tên, hãy tích chọn 'Xác nhận' bên dưới để tiếp tục lưu.");
                model.addAttribute("author", author);
                return "admin/author/add-author";
            }

            authorService.create(author);

            redirectAttributes.addFlashAttribute("successMessage", "Đã lưu tác giả thành công!");
            return "redirect:/authors";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Có lỗi hệ thống xảy ra: " + e.getMessage());
            return "admin/author/add-author";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        Author author = authorService.getById(id);
        if (author == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tác giả không tồn tại");
        }
        model.addAttribute("author", author);
        return "admin/author/edit-author";
    }

    @PostMapping("/edit/{id}")
    public String updateAuthor(@PathVariable int id,
                               @Valid @ModelAttribute("author") Author author,
                               BindingResult result,RedirectAttributes redirectAttributes,
                               @RequestParam(value = "confirm", required = false, defaultValue = "false") boolean confirm,
                               Model model) {

        if (result.hasErrors()) {
            return "admin/author/edit-author";
        }

        List<Author> exactMatches = authorService.searchByName(author.getName().trim())
                .stream()
                .filter(a -> a.getName().trim().equalsIgnoreCase(author.getName().trim()) && a.getId() != id)
                .toList();

        if (!exactMatches.isEmpty() && !confirm) {
            model.addAttribute("warningMessage", "Hệ thống phát hiện đã có tác giả khác tên '" + author.getName() + "' rồi. Nếu bạn vẫn muốn đặt tên này, hãy tích chọn 'Xác nhận' bên dưới.");
            model.addAttribute("author", author);
            return "admin/author/edit-author";
        }

        try {
            author.setId(id); // Đảm bảo ID được set trước khi update
            authorService.update(id, author);
            // Trong addAuthor và updateAuthor, trước khi redirect:
            redirectAttributes.addFlashAttribute("successMessage", "Đã lưu tác giả thành công!");
            return "redirect:/authors";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Cập nhật thất bại: " + e.getMessage());
            return "admin/author/edit-author";
        }
    }


    @GetMapping("/delete/{id}")
    public String deleteAuthor(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            authorService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa tác giả thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("foreign key")) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Không thể xóa tác giả này vì đang có các bộ truyện liên quan thuộc quyền sở hữu của họ!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi thực hiện xóa tác giả này.");
            }
        }
        return "redirect:/authors";
    }
}