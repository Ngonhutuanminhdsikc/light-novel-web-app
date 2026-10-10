package com.example._4.__thuc_hanh_quan_he.Controller;

import com.example._4.__thuc_hanh_quan_he.entity.*;
import com.example._4.__thuc_hanh_quan_he.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserAccountController {

    UserAccountService userAccountService;
    ReadHistoryService readHistoryService;
    JavaMailSender mailSender;
    FileService fileService;
    AuthorService authorService;
    TagService tagService;
    StoryService storyService;

    //đăng nhập
    @GetMapping("/login")
    public String showLoginForm() {
        return "admin/login";
    }

    //thoát
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Xóa sạch session
        return "redirect:/";
    }

    //đăng ký
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserAccount());
        return "admin/register";
    }

    //quên mật khẩu
    @GetMapping("/forgot")
    public String showForgotPasswordForm() {
        return "admin/forgot";
    }

    @PostMapping("/forgot")
    public String handleForgotPassword(@RequestParam("email") String email, Model model, HttpSession session) {
        UserAccount user = userAccountService.findByEmail(email);

        if (user != null) {
            try {
                String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
                session.setAttribute("otp", otp);
                session.setAttribute("emailReset", email);

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("Khôi phục mật khẩu - LN-MINH");
                message.setText("Mã xác nhận của bạn là: " + otp);

                mailSender.send(message);


                return "redirect:/verify-otp";
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error", "Gửi mail thất bại!");
            }
        } else {
            model.addAttribute("error", "Email không tồn tại!");
        }
        return "admin/forgot";
    }

    //lấy otp
    @GetMapping("/verify-otp")
    public String showVerifyOtp() {
        return "admin/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(@RequestParam("otp") String userOtp, HttpSession session, Model model) {
        String systemOtp = (String) session.getAttribute("otp");

        if (systemOtp != null && systemOtp.equals(userOtp)) {

            return "redirect:/reset-password";
        } else {
            model.addAttribute("error", "Mã OTP không đúng!");
            return "admin/verify-otp";
        }
    }

   //sửa mật khẩu
    @GetMapping("/reset-password")
    public String showResetPassword(HttpSession session) {
        if (session.getAttribute("emailReset") == null) {
            return "redirect:/forgot";
        }
        return "admin/reset-password";
    }


    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      HttpSession session,
                                      Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "admin/reset-password";
        }

        String email = (String) session.getAttribute("emailReset");
        System.out.println("DEBUG: Email lấy từ Session là: " + email);
        if (email != null) {
            System.out.println("DEBUG: Đang chuẩn bị gọi Service đổi mật khẩu...");
            userAccountService.updatePassword(email, password);

            SecurityContextHolder.clearContext();
            session.invalidate();
            // 5. Về trang login kèm thông báo thành công
            return "redirect:/login?resetSuccess=true";
        }

        return "redirect:/forgot";
    }

    //admin
    // Xử lý lưu User mới
    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("user") UserAccount user,
                                 BindingResult result,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", result.getAllErrors().get(0).getDefaultMessage());
            return "register";
        }

        if (!user.getPassWord().matches("^[a-zA-Z0-9@]+$")) {
            model.addAttribute("errorMessage", "Mật khẩu chỉ được chứa chữ, số và ký tự @");
            return "register";
        }

        try {
            userAccountService.createUser(user);
            return "redirect:/login?registerSuccess=true";
        } catch (Exception e) {
            e.printStackTrace();

            return "redirect:/register?error=true";
        }
    }

    @GetMapping("/user/profile")
    public String viewProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        UserAccount user = userAccountService.findByUserName(principal.getName());
        model.addAttribute("user", user);

        return "user/profile";
    }

    @GetMapping("/user/profile/edit")
    public String showUserProfileEditForm(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        UserAccount user = userAccountService.findByUserName(principal.getName());
        model.addAttribute("user", user);

        return "user/edit-profile";
    }

    @PostMapping("/user/profile/update")
    public String handleUserProfileUpdate(
            Principal principal,
            @RequestParam("email") String email,
            @RequestParam("fullName") String fullName,
            @RequestParam(value = "bio", required = false) String bio) {

        if (principal == null) return "redirect:/login";

        UserAccount existingUser = userAccountService.findByUserName(principal.getName());

        if (existingUser != null) {
            // Chỉ cập nhật nếu email hoặc tên mới có giá trị
            if (email != null && !email.isEmpty()) existingUser.setEmail(email);
            if (fullName != null) existingUser.setFullName(fullName);

            if (bio != null) {
                existingUser.setBio(bio);
            }

            userAccountService.updateUser(existingUser.getId(), existingUser);
        }

        return "redirect:/user/profile?updateSuccess=true";
    }

    //user
    // 1. Hiển thị trang lịch sử cho người dùng hiện tại
    @GetMapping("/history")
    public String showUserHistory(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        UserAccount user = userAccountService.findByUserName(principal.getName());

        model.addAttribute("histories", readHistoryService.getHistoryByUser(user.getId()));
        model.addAttribute("user", user);

        return "user/history";
    }

    // 2. Xử lý xóa một mục khỏi lịch sử
    @PostMapping("/history/delete")
    public String deleteHistory(@RequestParam("storyId") int storyId, Principal principal) {
        if (principal == null) return "redirect:/login";

        UserAccount user = userAccountService.findByUserName(principal.getName());
        readHistoryService.deleteByStory(user.getId(), storyId);

        return "redirect:/history";
    }


    //admin
    //Xem danh sách tất cả người dùng
    @GetMapping("/admin/users")
    public String listUsers(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<UserAccount> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userAccountService.searchUsers(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            users = userAccountService.getAllUsers();
        }
        model.addAttribute("users", users);
        return "admin/users/index"; // Đường dẫn của Minh
    }

    // 2. Hiển thị form chỉnh sửa Profile
    @GetMapping("/admin/users/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        model.addAttribute("user", userAccountService.getUserById(id));
        return "admin/users/edit-user";
    }

    // 3. Xử lý cập nhật Profile
    @PostMapping("/admin/users/update/{id}")
    public String handleUpdate(
            @PathVariable("id") int id,
            @RequestParam("email") String email,
            @RequestParam("roleId") int roleId) {


        UserAccount existingUser = userAccountService.getUserById(id);

        if (existingUser != null) {
            existingUser.setEmail(email);

            Role newRole = new Role();
            newRole.setId(roleId);
            existingUser.setRole(newRole);

            userAccountService.updateUser(id, existingUser);
        }

        return "redirect:/admin/users";
    }
    // 4. Thay đổi trạng thái tài khoản (Khóa/Mở khóa)
    @GetMapping("/admin/users/status/{id}")
    public String toggleStatus(@PathVariable int id, @RequestParam UserAccount.Status status) {
        userAccountService.changeStatus(id, status);
        return "redirect:/admin/users";
    }

    // 5. Xóa tài khoản
    @GetMapping("/admin/users/delete/{id}")
    public String handleDelete(@PathVariable int id, RedirectAttributes ra) {
        try {
            userAccountService.deleteUser(id);
            ra.addFlashAttribute("successMessage", "Xóa thành viên thành công!");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Lỗi này xảy ra khi tài khoản có ràng buộc khóa ngoại (ví dụ: đã đăng truyện, có lịch sử đọc...)
            ra.addFlashAttribute("errorMessage", "Không thể xóa vì thành viên này có dữ liệu liên quan (truyện, lịch sử đọc...)!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/user/profile/update-image")
    @ResponseBody
    public ResponseEntity<String> updateProfileImage(
            Principal principal,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            @RequestParam(value = "bannerFile", required = false) MultipartFile bannerFile) {

        if (principal == null) return ResponseEntity.status(401).body("Not logged in");

        try {
            UserAccount user = userAccountService.findByUserName(principal.getName());

            if (avatarFile != null && !avatarFile.isEmpty()) {
                user.setAvatarUrl("/uploads/" + fileService.saveImage(avatarFile));
            }

            if (bannerFile != null && !bannerFile.isEmpty()) {
                user.setBannerUrl("/uploads/" + fileService.saveImage(bannerFile));
            }

            userAccountService.updateUser(user.getId(), user);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error");
        }
    }


    //ĐĂNG TRUYỆN USER
    // 1. Hiển thị form đăng truyện
    @GetMapping("/post-story")
    public String showPostStoryForm(Model model) {
        Story story = new Story();
        story.setAuthor(new Author());
        model.addAttribute("story", story);


        model.addAttribute("tags", tagService.getAll());

        model.addAttribute("statuses", Story.Status.values());

        return "user/story/post-story";
    }

    // 2. Xử lý lưu truyện
    @PostMapping("/post-story")
    public String handlePostStory(
            @ModelAttribute("story") Story story,
            @RequestParam("authorName") String authorName,
            @RequestParam(value = "tagIds", required = false) List<Integer> tagIds,
            @RequestParam("imageFile") MultipartFile file,
            Principal principal
    ) throws IOException {
        try {

            Author author = authorService.findByName(authorName);
            if (author == null) {
                author = new Author();
                author.setName(authorName);
                author = authorService.save(author);
            }
            story.setAuthor(author);


            UserAccount currentUser = userAccountService.findByUserName(principal.getName());
            story.setUploader(currentUser);


            if (file != null && !file.isEmpty()) {
                story.setThumbnailUrl("/uploads/" + fileService.saveImage(file));
            }

            story.setApprovalStatus(Story.StoryStatus.PENDING);
            storyService.saveStoryWithTags(story, tagIds);

            return "redirect:/user/profile?postSuccess=true";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/post-story?error=true";
        }
    }
}