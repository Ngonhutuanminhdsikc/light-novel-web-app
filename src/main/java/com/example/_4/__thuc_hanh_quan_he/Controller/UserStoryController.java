package com.example._4.__thuc_hanh_quan_he.Controller;

import com.example._4.__thuc_hanh_quan_he.entity.Author;
import com.example._4.__thuc_hanh_quan_he.entity.Story;
import com.example._4.__thuc_hanh_quan_he.entity.UserAccount;
import com.example._4.__thuc_hanh_quan_he.service.*;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;

@Controller
@RequestMapping("/user/stories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserStoryController {

    StoryService storyService;
    TagService tagService;
    UserAccountService userAccountService;
    FileService fileService;
    AuthorService authorService;

    @GetMapping("/create-new-story")
    public String showAddForm(Model model) {
        try {
            List<Author> authors = authorService.getAll();

            System.out.println("DEBUG: Số lượng tác giả tìm được là: " + (authors == null ? "NULL" : authors.size()));

            model.addAttribute("story", new Story());
            model.addAttribute("tags", tagService.getAll());
            model.addAttribute("statuses", Story.Status.values());
            model.addAttribute("allAuthors", authors);

        } catch (Exception e) {
            System.err.println("LỖI KHI LẤY TÁC GIẢ: " + e.getMessage());
            e.printStackTrace();
        }
        return "user/story/post-story";
    }

    @PostMapping("/create-new-story")
    public String handlePostStory(
            @Valid @ModelAttribute("story") Story story,
            BindingResult result,
            @RequestParam(value = "authorId", required = false) Integer authorId,
            @RequestParam(value = "authorName", required = false) String authorName,
            @RequestParam(value = "tagIds", required = false) List<Integer> tagIds,
            @RequestParam("imageFile") MultipartFile file,
            Principal principal,
            Model model
    ) {

        if (result.hasErrors()) {
            model.addAttribute("story", story);
            model.addAttribute("tags", tagService.getAll());
            model.addAttribute("statuses", Story.Status.values());
            model.addAttribute("allAuthors", authorService.getAll());
            return "user/story/post-story";
        }

        try {

            UserAccount currentUser = userAccountService.findByUserName(principal.getName());
            story.setUploader(currentUser);
            story.setApprovalStatus(Story.StoryStatus.PENDING);

            Author author = null;
            if (authorId != null) {
                author = authorService.getById(authorId);
            } else if (authorName != null && !authorName.trim().isEmpty()) {
                author = authorService.findByName(authorName.trim());
                if (author == null) {
                    author = new Author();
                    author.setName(authorName.trim());
                    author = authorService.save(author);
                }
            }

            if (author == null) {
                model.addAttribute("errorMessage", "Vui lòng chọn hoặc nhập tên tác giả!");
                model.addAttribute("tags", tagService.getAll());
                model.addAttribute("statuses", Story.Status.values());
                model.addAttribute("allAuthors", authorService.getAll());
                return "user/story/post-story";
            }
            story.setAuthor(author);


            if (tagIds == null || tagIds.isEmpty()) {
                model.addAttribute("tagError", "Vui lòng chọn ít nhất một thể loại!");
                model.addAttribute("tags", tagService.getAll());
                model.addAttribute("statuses", Story.Status.values());
                model.addAttribute("allAuthors", authorService.getAll());
                return "user/story/post-story";
            }

            if (file != null && !file.isEmpty()) {
                story.setThumbnailUrl("/uploads/" + fileService.saveImage(file));
            }

            if (story.getTitle() != null) {
                String baseSlug = story.getTitle().toLowerCase().trim()
                        .replaceAll("[^a-z0-9]", "-")
                        .replaceAll("-+", "-");
                story.setSlug(baseSlug + "-" + System.currentTimeMillis());
            }

            storyService.saveStoryWithTags(story, tagIds);
            return "redirect:/user/stories/my-stories?postSuccess=true";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi lưu dữ liệu: " + e.getMessage());
            model.addAttribute("story", story);
            model.addAttribute("tags", tagService.getAll());
            model.addAttribute("statuses", Story.Status.values());
            model.addAttribute("allAuthors", authorService.getAll());
            return "user/story/post-story";
        }
    }

    // Lấy danh sách truyện của tôi
    @GetMapping("/my-stories")
    public String listMyStories(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        UserAccount currentUser = userAccountService.findByUserName(principal.getName());

        List<Story> myStories = storyService.getStoriesByUploader(currentUser.getId());

        model.addAttribute("stories", myStories);
        return "user/story/my-stories";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model, Principal principal) {
        Story story = storyService.getStoryById(id);

        UserAccount currentUser = userAccountService.findByUserName(principal.getName());

        if (story.getUploader() == null || !story.getUploader().getId().equals(currentUser.getId())) {
            return "redirect:/user/stories/my-stories?error=unauthorized";
        }

        if (story.getTags() == null) {
            story.setTags(new HashSet<>());
        }

        model.addAttribute("story", story);
        model.addAttribute("tags", tagService.getAll());
        model.addAttribute("statuses", Story.Status.values());
        model.addAttribute("allAuthors", authorService.getAll());

        model.addAttribute("authorError", null);
        model.addAttribute("tagError", null);

        return "user/story/edit-story";
    }


    @PostMapping("/edit/{id}")
    public String handleUpdateStory(
            @PathVariable("id") Integer id,
            @Valid @ModelAttribute("story") Story storyDetails,
            BindingResult result,
            @RequestParam(value = "authorId", required = false) Integer authorId,
            @RequestParam(value = "authorName", required = false) String authorName,
            @RequestParam(value = "tagIds", required = false) List<Integer> tagIds,
            @RequestParam("imageFile") MultipartFile file,
            Model model
    ) throws IOException {

        if (result.hasErrors()) {
            model.addAttribute("tags", tagService.getAll());
            model.addAttribute("statuses", Story.Status.values());
            model.addAttribute("allAuthors", authorService.getAll());
            return "user/story/edit-story";
        }

        try {
            Story existingStory = storyService.getStoryById(id);

            existingStory.setTitle(storyDetails.getTitle());
            existingStory.setDescription(storyDetails.getDescription());
            existingStory.setStatus(storyDetails.getStatus());

            Author author = null;
            if (authorId != null) {
                author = authorService.getById(authorId);
            } else if (authorName != null && !authorName.trim().isEmpty()) {
                author = authorService.findByName(authorName.trim());
                if (author == null) {
                    author = new Author();
                    author.setName(authorName.trim());
                    author = authorService.save(author);
                }
            }

            if (author == null) {
                model.addAttribute("authorError", "Bạn chưa chọn hoặc nhập tên tác giả!");
                model.addAttribute("tags", tagService.getAll());
                model.addAttribute("statuses", Story.Status.values());
                model.addAttribute("allAuthors", authorService.getAll());
                return "user/story/edit-story";
            }
            existingStory.setAuthor(author);

            if (tagIds == null || tagIds.isEmpty()) {
                model.addAttribute("tagError", "Vui lòng chọn ít nhất một thể loại!");
                model.addAttribute("tags", tagService.getAll());
                model.addAttribute("statuses", Story.Status.values());
                model.addAttribute("allAuthors", authorService.getAll());
                return "user/story/edit-story";
            }

            if (file != null && !file.isEmpty()) {
                existingStory.setThumbnailUrl("/uploads/" + fileService.saveImage(file));
            }

            storyService.saveStoryWithTags(existingStory, tagIds);

            return "redirect:/user/stories/my-stories?updateSuccess=true";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/user/stories/edit/" + id + "?error=true";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteStory(@PathVariable("id") Integer id,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {

            Story story = storyService.getStoryById(id);
            if (story == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Truyện không tồn tại!");
                return "redirect:/user/stories/my-stories";
            }

            UserAccount currentUser = userAccountService.findByUserName(principal.getName());
            if (!story.getUploader().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xóa truyện này!");
                return "redirect:/user/stories/my-stories";
            }

            storyService.deleteStory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa truyện thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa truyện: " + e.getMessage());
        }

        return "redirect:/user/stories/my-stories";
    }
}