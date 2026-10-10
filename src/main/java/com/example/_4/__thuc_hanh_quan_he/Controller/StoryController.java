package com.example._4.__thuc_hanh_quan_he.Controller;

import com.example._4.__thuc_hanh_quan_he.entity.*;
import com.example._4.__thuc_hanh_quan_he.service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StoryController {

    StoryService storyService;
    StoryStatsService storyStatsService;
    AuthorService authorService;
    TagService tagService;
    ChapterService chapterService;
    CommentService commentService;
    FollowService followService;
    UserAccountService userAccountService;


    // Admin
    // 1. Hiển thị danh sách truyện của Admin
    @GetMapping("/admin/stories")
    public String getAllStories(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Story> stories;

        if (keyword != null && !keyword.trim().isEmpty()) {
            stories = storyService.searchStories(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            stories = storyService.getAll();
        }

        model.addAttribute("stories", stories);
        return "admin/stories/stories-index";
    }

    // 2. Mở form thêm truyện mới
    @GetMapping("/admin/stories/add")
    public String showAddForm(Model model) {
        model.addAttribute("story", new Story());
        model.addAttribute("authors", authorService.getAll());
        model.addAttribute("tags", tagService.getAll());
        model.addAttribute("statuses", Story.Status.values());
        return "admin/stories/add-story";
    }

    @PostMapping("/admin/stories/add")
    public String handleAdd(@ModelAttribute("story") Story story,
                            @RequestParam(value = "tagIds", required = false) List<Integer> tagIds,
                            @RequestParam("imageFile") MultipartFile imageFile,
                            Model model) {
        try {

            if (story.getTitle() == null || story.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Tên bộ truyện không được để trống!");
            }
            if (story.getTitle().trim().length() < 2) {
                throw new IllegalArgumentException("Tên bộ truyện phải từ 2 đến 200 ký tự!");
            }

            if (story.getAuthor() == null || story.getAuthor().getId() == null) {
                throw new IllegalArgumentException("Vui lòng chọn tác giả!");
            }

            if (tagIds == null || tagIds.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng gán ít nhất một nhãn (Tag) cho bộ truyện!");
            }

            if (story.getTags() == null) {
                story.setTags(new HashSet<>());
            } else {
                story.getTags().clear();
            }

            for (Integer tId : tagIds) {
                Tag tag = tagService.getById(tId);
                if (tag != null) {
                    story.getTags().add(tag);
                }
            }

            if (story.getSlug() == null || story.getSlug().trim().isEmpty()) {
                String baseSlug = story.getTitle().toLowerCase().trim().replaceAll("[^a-z0-9]", "-");
                story.setSlug(baseSlug + "-" + System.currentTimeMillis());
            }

            handleImageUpload(story, imageFile);
            story.setApprovalStatus(Story.StoryStatus.PUBLISHED);
            storyService.create(story);

            return "redirect:/admin/stories";

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return refreshAddForm(model, story);

        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = "Có lỗi xảy ra trong quá trình lưu dữ liệu!";
            if (e.getMessage() != null && (e.getMessage().contains("Duplicate") || e.getMessage().contains("constraint"))) {
                errorMsg = "Đường dẫn (Slug) của truyện này đã tồn tại trên hệ thống!";
            }
            model.addAttribute("errorMessage", errorMsg);
            return refreshAddForm(model, story);
        }
    }

    private String refreshAddForm(Model model, Story story) {
        model.addAttribute("story", story);
        model.addAttribute("authors", authorService.getAll());
        model.addAttribute("tags", tagService.getAll());
        model.addAttribute("statuses", Story.Status.values());
        return "admin/stories/add-story";
    }

    // 4. Mở form chỉnh sửa
    @GetMapping("/admin/stories/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Story story = storyService.getById(id);
        model.addAttribute("story", story);
        model.addAttribute("authors", authorService.getAll());
        model.addAttribute("allTags", tagService.getAll());
        model.addAttribute("statuses", Story.Status.values());
        return "admin/stories/edit-story";
    }

    @PostMapping("/admin/stories/edit/{id}")
    public String handleUpdate(@PathVariable Integer id,
                               @ModelAttribute("story") Story storyFromForm,
                               @RequestParam(value = "tagIds", required = false) List<Integer> tagIds,
                               @RequestParam("imageFile") MultipartFile imageFile,
                               Model model) {
        Story existingStory = null;
        try {
            if (storyFromForm.getTitle() == null || storyFromForm.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Tên bộ truyện không được để trống!");
            }
            if (storyFromForm.getTitle().trim().length() < 2) {
                throw new IllegalArgumentException("Tên bộ truyện phải từ 2 đến 200 ký tự!");
            }

            if (tagIds == null || tagIds.isEmpty()) {
                throw new IllegalArgumentException("Bộ truyện bắt buộc phải có ít nhất một nhãn (Tag)!");
            }

            existingStory = storyService.getById(id);
            existingStory.setTitle(storyFromForm.getTitle());
            existingStory.setDescription(storyFromForm.getDescription());
            existingStory.setAuthor(storyFromForm.getAuthor());
            existingStory.setStatus(storyFromForm.getStatus());
            existingStory.setHot(storyFromForm.isHot());

            existingStory.getTags().clear();
            for (Integer tId : tagIds) {
                Tag tag = tagService.getById(tId);
                if (tag != null) {
                    existingStory.getTags().add(tag);
                }
            }

            handleImageUpload(existingStory, imageFile);
            storyService.update(id, existingStory);

            return "redirect:/admin/stories";

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return refreshEditForm(model, storyFromForm, id, existingStory);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = "Có lỗi xảy ra khi cập nhật thông tin!";
            if (e.getMessage() != null && (e.getMessage().contains("Duplicate") || e.getMessage().contains("constraint"))) {
                errorMsg = "Đường dẫn (Slug) này đã trùng lặp dữ liệu với một bộ truyện khác!";
            }
            model.addAttribute("errorMessage", errorMsg);
            return refreshEditForm(model, storyFromForm, id, existingStory);
        }
    }

    private String refreshEditForm(Model model, Story storyFromForm, Integer id, Story existingStory) {
        storyFromForm.setId(id);
        if (existingStory != null) {
            storyFromForm.setThumbnailUrl(existingStory.getThumbnailUrl());
        }
        model.addAttribute("story", storyFromForm);
        model.addAttribute("authors", authorService.getAll());
        model.addAttribute("allTags", tagService.getAll());
        model.addAttribute("statuses", Story.Status.values());
        return "admin/stories/edit-story";
    }

    // 6. Xóa truyện (Có thông báo thành công hoặc lỗi ràng buộc chương)
    @GetMapping("/admin/stories/delete/{id}")
    public String deleteStory(@PathVariable Integer id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            storyService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa bộ truyện thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra hoặc bộ truyện đang dính ràng buộc dữ liệu không thể xóa!");
        }
        return "redirect:/admin/stories";
    }


    // 7. Hiển thị trang danh sách truyện đang chờ duyệt
    @GetMapping("/admin/stories/pending")
    public String showPendingStories(Model model) {
        List<Story> pendingStories = storyService.getPendingStories();
        model.addAttribute("stories", pendingStories);
        return "admin/stories/pending-list";
    }

    // 8. Xử lý khi Admin bấm nút Phê duyệt hoặc Từ chối
    @PostMapping("/admin/stories/review/{id}")
    public String reviewStory(@PathVariable("id") Integer storyId,
                              @RequestParam("action") String action,
                              org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        boolean isUpdated = storyService.reviewStory(storyId, action);

        if (isUpdated) {
            if ("approve".equalsIgnoreCase(action)) {
                redirectAttributes.addFlashAttribute("successMessage", "Đã phê duyệt và xuất bản truyện thành công!");
            } else if ("reject".equalsIgnoreCase(action)) {
                redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối truyện này!");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy truyện yêu cầu hoặc có lỗi xảy ra.");
        }

        return "redirect:/admin/stories/pending";
    }

    private void handleImageUpload(Story story, MultipartFile imageFile) {
        if (!imageFile.isEmpty()) {
            try {
                String uploadDir = "src/main/resources/static/uploads/";
                String fileName = UUID.randomUUID().toString() + ".jpg";

                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                story.setThumbnailUrl("/uploads/" + fileName);

            } catch (IOException e) {
                System.out.println("Lỗi upload ảnh: " + e.getMessage());
            }
        }
    }



    //USER
    @GetMapping("/")
    public String index(Model model) {
        List<Story> hotStories = storyService.getHotStories();
        model.addAttribute("hotStories", hotStories);

        List<Story> recentStories = storyService.getNewestStories();
        model.addAttribute("recentStories", recentStories);

        List<Comment> latestComments = commentService.getLatestComments();
        model.addAttribute("latestComments", latestComments);

        model.addAttribute("completedStories", storyService.getCompletedStories());

        return "user/home";
    }

    @GetMapping("/story/{id}")
    public String showStoryDetail(@PathVariable("id") int id,
                                  Model model,
                                  HttpSession session,
                                  Principal principal) {

        Story story = storyService.getById(id);
        List<Chapter> chapters = chapterService.getChaptersByStory(id);


        boolean shouldIncrementView = true;

        if (principal != null) {
            String username = principal.getName();
            UserAccount currentUser = userAccountService.findByUserName(username);


            if (currentUser != null && story.getUploader() != null
                    && currentUser.getId().equals(story.getUploader().getId())) {
                shouldIncrementView = false;
            }
        }


        if (shouldIncrementView) {
            storyStatsService.incrementViewWithSession(id, session);
        }


        Map<Integer, List<Chapter>> chaptersByVol = chapters.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getVolNumber() != null ? c.getVolNumber() : 1,
                        TreeMap::new,
                        Collectors.toList()
                ));

        boolean isFollowing = false;
        if (principal != null) {
            String username = principal.getName();
            UserAccount currentUser = userAccountService.findByUserName(username);

            if (currentUser != null) {
                isFollowing = followService.isFollowing(currentUser.getId(), id);
            }
        }

        model.addAttribute("story", story);
        model.addAttribute("stats", storyStatsService.getStatsByStoryId(id));
        model.addAttribute("chaptersByVol", chaptersByVol);
        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("followCount", followService.countFollowers(id));
        List<Comment> comments = commentService.getCommentsByStory(id);
        model.addAttribute("comments", comments);

        return "user/story-detail";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "authorName", required = false) String authorName,
            @RequestParam(value = "tagId", required = false) Integer tagId,
            @RequestParam(value = "status", required = false) String status,
            Model model) {

        List<Story> results;

        try {

            results = storyService.advancedSearch(keyword, authorName, tagId, status);
        } catch (Exception e) {

            results = new ArrayList<>(); // Trả về danh sách trống

            model.addAttribute("errorMessage", "Từ khóa tìm kiếm hoặc bộ lọc không hợp lệ. Vui lòng thử lại!");
        }

        model.addAttribute("stories", results);
        model.addAttribute("keyword", keyword);
        model.addAttribute("authorName", authorName);
        model.addAttribute("selectedTag", tagId);
        model.addAttribute("selectedStatus", status);

        model.addAttribute("allTags", tagService.getAll());
        model.addAttribute("title", "Kết quả tìm kiếm nâng cao");
        model.addAttribute("isSearchPage", true);

        return "user/list_layout";
    }

    @GetMapping("/new")
    public String showLatestStories(Model model) {
        model.addAttribute("stories", storyService.getAllLatest());
        model.addAttribute("title", "Truyện mới nhất");
        model.addAttribute("isSearchPage", false);
        return "user/list_layout";
    }

    @GetMapping("/complete")
    public String showCompletedStories(Model model) {
        model.addAttribute("stories", storyService.getCompletedStories());
        model.addAttribute("title", "Truyện đã hoàn thành");
        model.addAttribute("isSearchPage", false);
        return "user/list_layout";
    }


    @GetMapping("/profile/{id}")
    public String showUserProfile(@PathVariable("id") int id, Model model) {
        UserAccount user = userAccountService.getUserById(id);

        if (user == null) {
            return "error/404";
        }

        model.addAttribute("user", user);

        return "user/profile";
    }
}