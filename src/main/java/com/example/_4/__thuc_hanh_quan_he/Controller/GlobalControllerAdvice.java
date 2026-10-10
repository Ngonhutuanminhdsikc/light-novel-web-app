package com.example._4.__thuc_hanh_quan_he.Controller;

import com.example._4.__thuc_hanh_quan_he.entity.UserAccount;
import com.example._4.__thuc_hanh_quan_he.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserAccountService userAccountService;

    @ModelAttribute
    public void addAttributes(Model model, Principal principal) {
        if (principal != null) {
            try {
                UserAccount user = userAccountService.findByUserName(principal.getName());
                model.addAttribute("user", user);
            } catch (Exception e) {
                // Log lỗi nhưng không làm crash ứng dụng
                System.err.println("Lỗi khi nạp thông tin user vào model: " + e.getMessage());
            }
        }
    }
}