package com.example._4.__thuc_hanh_quan_he.Controller;

import com.example._4.__thuc_hanh_quan_he.entity.Comment;
import com.example._4.__thuc_hanh_quan_he.service.CommentService;
import com.example._4.__thuc_hanh_quan_he.service.StoryService;
import com.example._4.__thuc_hanh_quan_he.service.UserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {

    CommentService commentService;
    StoryService storyService;
    UserAccountService userAccountService;

    //ADMIN
    @GetMapping("/story/{storyId}")
    public String listCommentsByStory(@PathVariable int storyId, Model model) {
        log.info("Admin xem danh sách bình luận của truyện ID: {}", storyId);
        model.addAttribute("story", storyService.getById(storyId));
        model.addAttribute("comments", commentService.getCommentsByStory(storyId));
        return "admin/comments-index";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addComment(@RequestParam("storyId") int storyId,
                                        @RequestParam("content") String content,
                                        Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập");
        }

        try {

            var user = userAccountService.findByUserName(principal.getName());
            var story = storyService.getById(storyId);

            Comment newComment = new Comment();
            newComment.setContent(content);
            newComment.setStory(story);
            newComment.setUser(user);
            newComment.setCreatedAt(LocalDateTime.now());

            commentService.save(newComment);

            log.info("Lưu bình luận thành công cho truyện ID: {}", storyId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Lỗi lưu bình luận: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteComment(@PathVariable int id, Principal principal, HttpServletRequest request) {
        Comment comment = commentService.getCommentById(id);
        if (comment == null) {
            return ResponseEntity.status(404).body("Không tìm thấy bình luận");
        }

        boolean isOwner = comment.getUser().getUserName().equals(principal.getName());
        boolean isAdmin = request.isUserInRole("ROLE_ADMIN");

        if (isOwner || isAdmin) {
            commentService.deleteComment(id);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(403).body("Bạn không có quyền xóa");
    }


    @GetMapping("/latest")
    public String getLatestCommentsForHome(Model model) {
        log.info("User đang tải danh sách bình luận mới nhất ra trang chủ");


        model.addAttribute("latestComments", commentService.getLatestComments());

        return "user/index :: #comment-section";
    }

    // SỬA BÌNH LUẬN
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateComment(@RequestParam("id") int id,
                                           @RequestParam("content") String content,
                                           Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Vui lòng đăng nhập");

        Comment comment = commentService.getCommentById(id);


        if (comment != null && comment.getUser().getUserName().equals(principal.getName())) {
            comment.setContent(content);
            comment.setCreatedAt(LocalDateTime.now());
            commentService.update(comment);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(403).body("Bạn không có quyền sửa bình luận này");
    }
}