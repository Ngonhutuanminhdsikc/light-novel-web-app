package com.example._4.__thuc_hanh_quan_he.Controller;

import com.example._4.__thuc_hanh_quan_he.entity.Role;
import com.example._4.__thuc_hanh_quan_he.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/rolesController")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {

    final RoleService roleService;

    // 1. Hiển thị danh sách tất cả vai trò
    @GetMapping
    public String listRoles(Model model) {
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin/roles-index";
    }

    // 2. Form thêm vai trò mới
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("role", new Role());
        return "admin/add-role";
    }

    // 3. Xử lý lưu vai trò mới
    @PostMapping("/add")
    public String handleAdd(@ModelAttribute("role") Role role) {
        roleService.createRole(role);
        return "redirect:/rolesController";
    }

    // 4. Form chỉnh sửa vai trò
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        model.addAttribute("role", roleService.getRoleById(id));
        return "admin/edit-role";
    }

    // 5. Xử lý cập nhật
    @PostMapping("/edit/{id}")
    public String handleUpdate(@PathVariable int id, @ModelAttribute("role") Role role) {
        roleService.updateRole(id, role);
        return "redirect:/rolesController";
    }

    @GetMapping("/delete/{id}")
    public String handleDelete(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            roleService.deleteRole(id);
            redirectAttributes.addFlashAttribute("success", "Xóa vai trò thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa: Vai trò này đang được sử dụng!");
        }
        return "redirect:/rolesController";
    }
}