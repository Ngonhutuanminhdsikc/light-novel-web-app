package com.example._4.__thuc_hanh_quan_he.Controller;

import com.example._4.__thuc_hanh_quan_he.entity.ReadHistory;
import com.example._4.__thuc_hanh_quan_he.entity.UserAccount;
import com.example._4.__thuc_hanh_quan_he.service.ReadHistoryService;
import com.example._4.__thuc_hanh_quan_he.service.UserAccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
@RequestMapping("/historyController")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReadHistoryController {

    ReadHistoryService readHistoryService;
    UserAccountService userService;

    // 1. Xem lịch sử đọc
    @GetMapping("/user/{userId}")
    public String listHistoryByUser(@PathVariable int userId, Model model) {
        model.addAttribute("user", userService.getUserById(userId));
        model.addAttribute("histories", readHistoryService.getHistoryByUser(userId));
        return "admin/user-history";
    }


    @GetMapping("/my-history")
    public String myHistory(Model model, Principal principal) {
        if (principal != null) {
            UserAccount user = userService.findByUserName(principal.getName());
            model.addAttribute("histories", readHistoryService.getHistoryByUser(user.getId()));
            return "user/history";
        }
        return "redirect:/login";
    }


    @PostMapping("/delete/{storyId}")
    public String deleteMyHistory(@PathVariable int storyId, Principal principal) {
        if (principal == null) return "redirect:/login";

        UserAccount currentUser = userService.findByUserName(principal.getName());

        if (currentUser != null) {
            readHistoryService.deleteByStory(currentUser.getId(), storyId);
        }

        readHistoryService.deleteByStory(currentUser.getId(), storyId);

        return "redirect:/historyController/my-history";
    }

}