package com.example._4.__thuc_hanh_quan_he.Controller;

import com.example._4.__thuc_hanh_quan_he.entity.UserAccount;
import com.example._4.__thuc_hanh_quan_he.service.FollowService;
import com.example._4.__thuc_hanh_quan_he.service.StoryService;
import com.example._4.__thuc_hanh_quan_he.service.UserAccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/follows") // Đổi tên cho ngắn gọn
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FollowController {

    FollowService followService;
    UserAccountService userService;


    // 1. Xem danh sách truyện đã theo dõi
    @GetMapping("/user/{userId}")
    public String listFollowsByUser(@PathVariable Integer userId, Model model) {
        model.addAttribute("user", userService.getUserById(userId));
        model.addAttribute("follows", followService.getFollowsByUser(userId));
        return "admin/user-follows";
    }

    // 2. Xử lý Toggle Follow (Hàm này dùng chung cho cả nút Follow và Unfollow)
    @PostMapping("/toggle/{storyId}")
    public ResponseEntity<?> toggleFollow(@PathVariable Integer storyId, Principal principal) {


        if (principal == null) {
            return ResponseEntity.status(401).body("Bạn cần đăng nhập để thực hiện thao tác này");
        }

        UserAccount currentUser = userService.findByUserName(principal.getName());

        String result = followService.toggleFollow(currentUser.getId(), storyId);

        long newCount = followService.countFollowers(storyId);


        Map<String, Object> response = new HashMap<>();
        response.put("status", result);
        response.put("newCount", newCount);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/tu-truyen")
    public String viewLibrary(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        UserAccount currentUser = userService.findByUserName(principal.getName());
        if (currentUser == null) return "redirect:/logout";

        model.addAttribute("follows", followService.getFollowsByUser(currentUser.getId()));
        model.addAttribute("user", currentUser);

        return "user/library";
    }
}